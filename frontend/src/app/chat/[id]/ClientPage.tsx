'use client';

import { useEffect, useState, useRef, use } from 'react';
import { useParams } from 'next/navigation';
import { Users, Upload, Download } from 'lucide-react';
import axios from 'axios';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { LoginMemberContext } from "@/stores/auth/LoginMember";
import { v4 as uuidv4 } from 'uuid'; // UUID 패키지 임포트

interface Message {
  id: string;
  chatRoomId: number;
  senderId: number;
  senderNickname: string;
  content: string;
  fileUrls: string[];
  type: string;
  createdAt: string;
}

interface ChatRoomDetail {
  chatRoomId: number;
  group: {
    groupId: number;
    groupName: string;
    participantCount: number;
  };
  members: {
    memberId: number;
    memberNickname: string;
    groupRole: string;
  }[];
}

export default function ChatRoom() {
    const params = useParams();
    const chatRoomId = params.id as string;
    const { loginMember } =use(LoginMemberContext);
    const [chatRoomDetail, setChatRoomDetail] = useState<ChatRoomDetail | null>(null);
    const [messages, setMessages] = useState<Message[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [newMessage, setNewMessage] = useState('');
    const clientRef = useRef<Client | null>(null);
    const messagesEndRef = useRef<HTMLDivElement>(null);
    const [page, setPage] = useState(0);
    const [hasMore, setHasMore] = useState(true);
    const [isLoadingMore, setIsLoadingMore] = useState(false);
    const [shouldScrollToBottom, setShouldScrollToBottom] = useState(true);

    const fileInputRef = useRef<HTMLInputElement | null>(null); // 파일 입력 참조
    const [selectedFiles, setSelectedFiles] = useState<File[]>([]);
    const [showFileInfo, setShowFileInfo] = useState(false); // 파일 정보 토글 상태

    const scrollToBottom = () => {
        messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
    };

    const fetchMessages = async (pageNum: number) => {
        try {
            const token = localStorage.getItem('accessToken');
            if (!token) throw new Error('인증 토큰이 없습니다.');

            const config = {
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json'
                },
                withCredentials: true
            };

            const messagesResponse = await axios.get(
                `http://localhost:8080/api/v1/chatrooms/${chatRoomId}/messages?page=${pageNum}`, 
                config
            );
            
            const newMessages = messagesResponse.data.data.content;
            const isLast = messagesResponse.data.data.last;

            setMessages(prev => {
                const uniqueMessages = [...prev, ...newMessages.reverse()];
                const seen = new Set();
                return uniqueMessages.filter(message => {
                    const duplicate = seen.has(message.id);
                    seen.add(message.id);
                    return !duplicate;
                });
            });
            setHasMore(!isLast);
            setIsLoadingMore(false);
        } catch (error) {
            console.error('메시지 조회 실패:', error);
            setIsLoadingMore(false);
        }
    };

    const handleScroll = (e: React.UIEvent<HTMLDivElement>) => {
        const element = e.target as HTMLDivElement;
        if (element.scrollTop === 0 && hasMore && !isLoadingMore) {
            setIsLoadingMore(true);
            setShouldScrollToBottom(false);
            setPage(prev => prev + 1);
            fetchMessages(page + 1);
        }
    };

    // 소켓 연결
    const connectWebSocket = (chatRoomId: string | string[]) => {
        const token = localStorage.getItem('accessToken');
        const socket = new SockJS(`http://localhost:8080/ws/chat?token=${token}`);
        const client = new Client({
            webSocketFactory: () => socket,
            connectHeaders: {
                'Authorization': `Bearer ${token}`
            },
            onConnect: () => {
                // 새 메시지는 배열 앞에 추가
                client.subscribe(`/exchange/chat.exchange/chat.${chatRoomId}`, (message) => {
                    const receivedMessage: Message = JSON.parse(message.body);
                    setMessages((prevMessages) => [receivedMessage, ...prevMessages]);
                    setShouldScrollToBottom(true);
                });
                console.log(`웹소켓 연결 성공 -> chatRoom: ${chatRoomId}`);
            },
            onStompError: (frame) => {
                console.error('Broker reported error: ' + frame.headers['message']);
                console.error('Additional details: ' + frame.body);
            }
        });

        client.activate();
        clientRef.current = client;
        return socket
    };

    const websocketRef = useRef<WebSocket>(null)

    const disconnectWebSocket = () => {
        if (clientRef.current) {
            console.log(`웹소켓 연결 종료 -> chatRoom: ${chatRoomId}`);
            clientRef.current.deactivate(); // 웹소켓 연결 종료
            clientRef.current = null; // 클라이언트 참조 초기화
        }
    };

    useEffect(() => {
        // 이전 메시지를 불러오는 중이 아니고, shouldScrollToBottom이 true일 때만 스크롤
        if (!isLoadingMore && shouldScrollToBottom) {
            scrollToBottom();
        }
    }, [messages, isLoadingMore, shouldScrollToBottom]);

    useEffect(() => {
        if (websocketRef.current) return;
        const fetchChatRoomData = async () => {
            try {
                const token = localStorage.getItem('accessToken');

                // axios 요청 설정
                const config = {
                    headers: {
                        'Authorization': `Bearer ${token}`,
                        'Content-Type': 'application/json'
                    },
                    withCredentials: true
                };

                // 채팅방 정보 조회
                const roomResponse = await axios.get(`http://localhost:8080/api/v1/chatrooms/${chatRoomId}`, config);
                setChatRoomDetail(roomResponse.data.data);
        
                // 메시지 목록 조회
                await fetchMessages(0);
            } catch (error) {
                console.error('데이터 조회 실패:', error);
            } finally {
                setIsLoading(false);
            }
        };
    
        fetchChatRoomData();
        // 웹소켓 연결
        const socket = connectWebSocket(chatRoomId);
        websocketRef.current = socket;

        // cleanup 함수 수정
        return () => {
            // if (clientRef.current) {
            //     console.log(`웹소켓 연결 종료 -> chatRoom: ${chatRoomId}`);
            //     clientRef.current.deactivate();
            //     clientRef.current = null;
            // }
        };
    }, [chatRoomId]);

    const handleImageUpload = () => {
        fileInputRef.current?.click(); // 파일 선택 대화상자 열기
    };

    const handleFileChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        const files = event.target.files;
        if (files) {
            const fileArray = Array.from(files);
            const newFiles = fileArray.map((file) => {
                const existingFile = selectedFiles.find(f => f.name === file.name);
                if (existingFile) {
                    return new File([file], `${uuidv4()}_${file.name}`, { type: file.type });
                }
                return file;
            });
            setSelectedFiles(prevFiles => [...prevFiles, ...newFiles]);
            setShowFileInfo(true); // 파일 정보 창 열기
        }
    };

    const handleDragLeave = (event: React.DragEvent<HTMLDivElement>) => {
        event.preventDefault();
    };

    const handleDragOver = (event: React.DragEvent<HTMLDivElement>) => {
        event.preventDefault(); // 기본 동작 방지
    };

    const handleDrop = (event: React.DragEvent<HTMLDivElement>) => {
        event.preventDefault(); // 기본 동작 방지
        const files = event.dataTransfer.files;
        if (files) {
            handleFileChange({ target: { files } } as React.ChangeEvent<HTMLInputElement>);
        }
    };

    const handleCloseFileInfo = () => {
        setShowFileInfo(false); // 파일 정보 창 닫기
        setSelectedFiles([]); // 선택된 파일 목록 초기화
    };

    if (isLoading) {
        return <div className="flex justify-center items-center h-screen">로딩 중...</div>;
    }

    const sendMessage = () => {
        if (!newMessage.trim() || !clientRef.current) return;

        const messageToSend = {
            chatRoomId: chatRoomId,
            senderId: loginMember.id,
            senderNickname: loginMember.nickname,
            content: newMessage,
            type: "text"
        };

        clientRef.current.publish({
            destination: `/pub/chat.${chatRoomId}`,
            body: JSON.stringify(messageToSend)
        });

        setNewMessage('');
    };

    const sendFile = async () => {
        console.log("sendFile 함수 호출됨"); // 함수 호출 확인
        if (!clientRef.current) {
            console.error("STOMP 클라이언트가 없습니다."); // 클라이언트가 없을 때 로그
            return; // 클라이언트가 없으면 종료
        }
        
        if (selectedFiles.length === 0) {
            console.error("선택된 파일이 없습니다."); // 선택된 파일이 없을 때 로그
            return; // 선택된 파일이 없으면 종료
        }

        try {
            const fileUrls: string[] = []; // 업로드된 파일 URL을 저장할 배열

            for (const file of selectedFiles) {
                console.log(`Signed URL 요청 시작: ${file.name}`); // Signed URL 요청 로그
                const signedUrlResponse = await fetch(`http://localhost:8080/api/v1/generate-signed-url?filename=${uuidv4()}_${file.name}`, { // UUID 추가
                    method: 'GET',
                    headers: {
                        'Authorization': `Bearer ${localStorage.getItem('accessToken')}` // 로컬 스토리지에서 토큰 가져오기
                    }
                });

                if (!signedUrlResponse.ok) {
                    throw new Error('Signed URL 요청 실패');
                }

                const signedUrl = await signedUrlResponse.text(); // Signed URL 받기
                console.log("Signed URL:", signedUrl); // Signed URL 로그

                const uploadResponse = await fetch(signedUrl, {
                    method: 'PUT',
                    body: file, // 파일을 PUT 요청으로 전송
                    headers: {
                        'Content-Type': file.type, // 파일의 MIME 타입 설정
                        'Content-Disposition': 'attachment' // 다운로드로 강제하기 위해 설정
                    }
                });

                if (!uploadResponse.ok) {
                    throw new Error('파일 업로드 실패');
                }

                const publicUrl = signedUrl.split('?')[0]; // Signed URL에서 실제 URL 추출
                console.log("업로드된 파일 URL:", publicUrl); // 업로드된 파일 URL 로그
                fileUrls.push(publicUrl); // 업로드된 파일 URL을 배열에 추가
            }

            // STOMP 메시지 전송 로직 추가 (파일 URL 포함)
            const messageToSend = {
                chatRoomId: chatRoomId,
                senderId: loginMember.id,
                senderNickname: loginMember.nickname,
                content: newMessage, // 기존 메시지 내용
                fileUrls: fileUrls, // 업로드된 파일 URL 배열 추가
                type: "file"
            };

            console.log("전송할 메시지:", messageToSend); // 전송할 메시지 로그

            // STOMP 메시지 전송
            clientRef.current.publish({
                destination: `/pub/chat.${chatRoomId}`,
                body: JSON.stringify(messageToSend)
            });

            console.log("메시지 전송 완료"); // 메시지 전송 완료 로그

            // 파일 전송 후 상태 초기화
            setSelectedFiles([]);
            setShowFileInfo(false); // 파일 정보 창 닫기
        } catch (error) {
            console.error("파일 전송 중 오류 발생:", error); // 오류 로그 출력
        }
    };

    // 날짜 포맷팅 함수
    const formatDate = (date: Date) => {
        return date.toLocaleDateString('ko-KR', {
            year: 'numeric',
            month: 'long',
            day: 'numeric'
        });
    };

    // 시간 포맷팅 함수
    const formatTime = (date: Date) => {
        return date.toLocaleTimeString('ko-KR', {
            hour: '2-digit',
            minute: '2-digit',
            hour12: false
        });
    };

    // 메시지를 날짜별로 그룹화하는 함수
    const groupMessagesByDate = (messages: Message[]) => {
        const groups: { [key: string]: Message[] } = {};
        
        // 배열을 뒤집어서 처리
        [...messages].reverse().forEach(message => {
            const date = new Date(message.createdAt);
            const dateStr = formatDate(date);
            if (!groups[dateStr]) {
                groups[dateStr] = [];
            }
            groups[dateStr].push(message);
        });
        
        return groups;
    };

    const renderFilePreview = (file: File) => {
        const fileType = file.type.split('/')[0]; // 파일 타입 확인
        const iconSize = '50px'; // 아이콘 및 이미지 크기 설정

        if (fileType === 'image') {
            return <img src={URL.createObjectURL(file)} alt={file.name} style={{ width: iconSize, height: iconSize, objectFit: 'cover', marginRight: '10px' }} />;
        } else {
            // 파일 확장자에 따라 다른 아이콘 표시
            const fileExtension = file.name.split('.').pop()?.toLowerCase();
            let icon;

            switch (fileExtension) {
                case 'mp4':
                case 'avi':
                case 'mov':
                    icon = '🎥'; // 동영상 아이콘
                    break;
                case 'ppt':
                case 'pptx':
                    icon = '📊'; // PPT 아이콘
                    break;
                case 'txt':
                    icon = '📄'; // 텍스트 파일 아이콘
                    break;
                default:
                    icon = '📁'; // 기본 파일 아이콘
                    break;
            }

            return <span style={{ fontSize: iconSize, marginRight: '10px' }}>{icon}</span>; // 파일 아이콘만 표시
        }
    };

    return (
        <div className="flex flex-col h-[calc(100vh-4rem)] relative">
            {/* 드래그 앤 드롭 영역 */}
            <div 
                onDrop={handleDrop} 
                onDragOver={handleDragOver} 
                onDragLeave={handleDragLeave} // 드래그가 취소되었을 때 호출
                className="absolute inset-0 p-4 flex items-center justify-center"
                style={{ zIndex: 0, pointerEvents: 'none' }} // 클릭 이벤트 무시하고 채팅 화면 뒤로 보내기
            >
                {/* 드래그 앤 드롭 안내 텍스트를 숨기고 싶다면 아래 주석을 해제하세요 */}
                {/* <p>여기에 파일을 드래그 앤 드롭하세요</p> */}
            </div>

            {/* 채팅방 헤더 */}
            <div className="bg-white dark:bg-gray-800 border-b p-4 flex-shrink-0 z-10"> {/* z-index 추가 */}
                <h1 className="text-xl font-bold dark:text-white">{chatRoomDetail?.group.groupName}</h1>
                <p className="text-sm text-gray-500 dark:text-gray-400 flex items-center gap-1">
                    <Users className="w-4 h-4" />
                    {chatRoomDetail?.group.participantCount}명
                </p>
            </div>
    
            {/* 메시지 목록 */}
            <div className="flex-1 overflow-y-auto p-4 space-y-4 min-h-0" onScroll={handleScroll}>
                {isLoadingMore && (
                    <div className="text-center py-2">이전 메시지를 불러오는 중...</div>
                )}
                {Object.entries(groupMessagesByDate(messages)).map(([date, dateMessages]) => (
                    <div key={date}>
                        {/* 날짜 구분선 */}
                        <div className="flex items-center my-4">
                            <div className="flex-1 border-t border-gray-300 dark:border-gray-600"></div>
                            <span className="mx-4 text-sm text-gray-500 dark:text-gray-400">{date}</span>
                            <div className="flex-1 border-t border-gray-300 dark:border-gray-600"></div>
                        </div>
                        
                        {/* 해당 날짜의 메시지들 */}
                        {dateMessages.map((message: Message, i) => {
                            const isMyMessage = message.senderId === loginMember.id;
                            const showTime = i === dateMessages.length - 1 || 
                                            formatTime(new Date(message.createdAt)) !== formatTime(new Date(dateMessages[i + 1].createdAt));

                            return (
                                <div key={message.id} 
                                    className={`flex flex-col mb-2 ${isMyMessage ? 'items-end' : 'items-start'}`}>
                                    <span className="font-bold dark:text-white mb-1">{!isMyMessage && message.senderNickname}</span>
                                    <div className={`flex items-end gap-2 ${isMyMessage ? 'flex-row-reverse' : ''}`}>
                                        {message.type === 'file' ? (
                                            // 파일 메시지 내용 (이미지)
                                            <div className="flex flex-wrap">
                                                {message.fileUrls.map((url, index) => (
                                                    <div key={index} className="m-1 flex items-center relative">
                                                        <img 
                                                            src={url} 
                                                            alt={`Uploaded file ${index}`} 
                                                            style={{ width: '150px', height: '150px', objectFit: 'cover', cursor: 'pointer' }} 
                                                            onClick={() => window.open(url)} // 클릭 시 이미지 확대
                                                        />
                                                        <a href={url} download>
                                                            <button 
                                                                className="absolute bottom-0 right-1 bg-gray-500 text-black px-2 py-1 rounded-lg flex items-center" // right-2로 오른쪽에서 띄움
                                                                title="Download"
                                                            >
                                                                <Download className="w-4 h-4" />
                                                            </button>
                                                        </a>
                                                    </div>
                                                ))}
                                            </div>
                                        ) : (
                                            // 일반 메시지 내용
                                            <p className={`rounded-lg p-2 max-w-[100%] whitespace-pre-wrap
                                                ${isMyMessage 
                                                    ? 'bg-blue-100 text-blue-900 dark:bg-blue-900 dark:text-blue-100' 
                                                    : 'bg-gray-100 text-gray-900 dark:bg-gray-700 dark:text-gray-100'}`}>
                                                {message.content}
                                            </p>
                                        )}
                                        {showTime && (
                                            <span className="text-xs text-gray-500 dark:text-gray-400">
                                                {formatTime(new Date(message.createdAt))}
                                            </span>
                                        )}
                                    </div>
                                </div>
                            );
                        })}
                    </div>
                ))}
                <div ref={messagesEndRef} />
            </div>
    
            
            {/* 파일 정보 토글 창 */}
            {showFileInfo && (
                <div style={{
                    position: 'fixed',
                    top: '50%',
                    left: '50%',
                    transform: 'translate(-50%, -50%)',
                    border: '1px solid #ccc',
                    padding: '20px',
                    backgroundColor: 'white',
                    boxShadow: '0 4px 8px rgba(0, 0, 0, 0.2)',
                    zIndex: 1000 // 다른 요소 위에 표시
                }}>
                    <button 
                        onClick={handleCloseFileInfo}
                        style={{
                            position: 'absolute',
                            top: '10px',
                            right: '10px',
                            border: 'none',
                            background: 'none',
                            fontSize: '20px',
                            cursor: 'pointer'
                        }}
                    >
                        &times; {/* X 표시 */}
                    </button>
                    <h4>파일 전송</h4>
                    <ul style={{ listStyleType: 'none', padding: 0 }}>
                        {selectedFiles.map((file, index) => (
                            <li key={index} style={{ display: 'flex', alignItems: 'center', marginBottom: '10px' }}>
                                {renderFilePreview(file)}
                                <span>{file.name} - {Math.round(file.size / 1024)} KB</span> {/* 파일 이름과 크기 표시 */}
                            </li>
                        ))}
                    </ul>
                    <div style={{ textAlign: 'center', marginTop: '20px' }}>
                        <button 
                            onClick={sendFile} 
                            style={{
                                width: '100%', // 버튼을 가로로 길게 설정
                                padding: '10px',
                                backgroundColor: '#f0f0f0', // 덜 눈에 띄는 색상
                                color: '#333',
                                border: '1px solid #ccc',
                                borderRadius: '5px',
                                cursor: 'pointer',
                                fontSize: '16px',
                                transition: 'background-color 0.3s',
                                margin: '0' // 최소한의 마진 설정
                            }}
                            onMouseEnter={(e) => e.currentTarget.style.backgroundColor = '#e0e0e0'} // 마우스 오버 시 색상 변경
                            onMouseLeave={(e) => e.currentTarget.style.backgroundColor = '#f0f0f0'} // 마우스 나가면 원래 색상으로
                        >
                            전송
                        </button>
                    </div>
                </div>
            )}
            {/* 메시지 입력 */}
            <div className="border-t p-4 flex-shrink-0 bg-white dark:bg-gray-800 z-10"> {/* z-index 추가 */}
                <div className="flex gap-2 items-center">
                    <Upload className="w-6 h-6 cursor-pointer" onClick={handleImageUpload} />
                    <input
                        type="file"
                        ref={fileInputRef}
                        onChange={handleFileChange}
                        style={{ display: 'none' }} // 파일 입력 숨기기
                        multiple // 다중 선택 허용
                    />
                    <input
                        type="text"
                        value={newMessage}
                        onChange={(e) => setNewMessage(e.target.value)}
                        onKeyPress={(e) => e.key === 'Enter' && sendMessage()}
                        className="flex-1 border rounded-lg px-4 py-2 dark:bg-gray-700 dark:border-gray-600 dark:text-white dark:placeholder-gray-400"
                        placeholder="메시지를 입력하세요"
                    />
                    <button 
                        onClick={sendMessage}
                        className="bg-blue-500 text-white px-4 py-2 rounded-lg hover:bg-blue-600"
                    >
                        전송
                    </button>
                </div>
            </div>

            {/* 다운로드 아이콘 추가 */}
            {/* {selectedFiles.length > 0 && (
                <div className="absolute bottom-4 left-4">
                    <button 
                        className="bg-gray-300 text-black px-4 py-2 rounded-lg"
                        onClick={() => {
                            // 다운로드 로직 추가
                            selectedFiles.forEach(file => {
                                const url = URL.createObjectURL(file);
                                const a = document.createElement('a');
                                a.href = url;
                                a.download = file.name;
                                document.body.appendChild(a);
                                a.click();
                                document.body.removeChild(a);
                            });
                        }}
                    >
                        모든 파일 다운로드
                    </button>
                </div>
            )} */}
        </div>
    );
}