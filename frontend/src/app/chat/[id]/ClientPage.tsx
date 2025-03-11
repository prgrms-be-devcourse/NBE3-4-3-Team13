'use client';

import { useEffect, useState, useRef, use } from 'react';
import { useParams } from 'next/navigation';
import { Users, Upload } from 'lucide-react';
import axios from 'axios';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { LoginMemberContext } from "@/stores/auth/LoginMember";

interface Message {
  id: string;
  chatRoomId: number;
  senderId: number;
  senderNickname: string;
  content: string;
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
        };

        clientRef.current.publish({
            destination: `/pub/chat.${chatRoomId}`,
            body: JSON.stringify(messageToSend)
        });

        setNewMessage('');
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

    return (
        <div className="flex flex-col h-[calc(100vh-4rem)]">
            {/* 채팅방 헤더 */}
            <div className="bg-white dark:bg-gray-800 border-b p-4 flex-shrink-0">
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
                            
                            // 30자마다 줄바꿈 문자 추가
                            const formattedContent = message.content.match(/.{1,30}/g)?.join('\n') || message.content;
                            
                            return (
                                <div key={message.id} 
                                    className={`flex flex-col mb-2 ${isMyMessage ? 'items-end' : 'items-start'}`}>
                                    <span className="font-bold dark:text-white mb-1">{!isMyMessage && message.senderNickname}</span>
                                    <div className={`flex items-end gap-2 ${isMyMessage ? 'flex-row-reverse' : ''}`}>
                                        <p className={`rounded-lg p-2 max-w-[100%] whitespace-pre-wrap
                                            ${isMyMessage 
                                                ? 'bg-blue-100 text-blue-900 dark:bg-blue-900 dark:text-blue-100' 
                                                : 'bg-gray-100 text-gray-900 dark:bg-gray-700 dark:text-gray-100'}`}>
                                            {formattedContent}
                                        </p>
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
    
            {/* 메시지 입력 */}
            <div className="border-t p-4 flex-shrink-0 bg-white dark:bg-gray-800">
                <div className="flex gap-2 items-center">
                    {/* // 이미지 및 문서 업로드 아이콘 추가 */}
                    <Upload className="w-6 h-6 cursor-pointer"  />
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
        </div>
    );
}