"use client";

import { useEffect, useState } from "react";
import { Users } from 'lucide-react';

interface ChatRoom {
  chatRoomId: number;
  groupId: number;
  groupName: string;
  participant: number;
}

interface ApiResponse {
  isSuccess: boolean;
  code: string;
  message: string;
  data: ChatRoom[];
}

export default function ChatPage() {
  const [chatRooms, setChatRooms] = useState<ChatRoom[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchChatRooms = async () => {
      try {
        const token = localStorage.getItem("accessToken");
        const response = await fetch("http://localhost:8080/api/v1/members/chatrooms", {
          headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
          },
          credentials: 'include'
        });

        if (!response.ok) {
          throw new Error('채팅방 목록을 불러오는데 실패했습니다.');
        }

        const data: ApiResponse = await response.json();
        if (data.isSuccess) {
          setChatRooms(data.data);
        } else {
          setError(data.message);
        }
      } catch (err) {
        setError(err instanceof Error ? err.message : '알 수 없는 에러가 발생했습니다.');
      } finally {
        setIsLoading(false);
      }
    };

    fetchChatRooms();
  }, []);

  if (isLoading) {
    return <div className="flex justify-center items-center h-full">로딩중...</div>;
  }

  if (error) {
    return <div className="flex justify-center items-center h-full text-red-500">{error}</div>;
  }

  return (
    <div className="container mx-auto p-4">
      <h1 className="text-2xl font-bold mb-4">채팅방 목록</h1>
      <div className="space-y-4">
        {chatRooms.length === 0 ? (
          <p className="text-center text-gray-500">참여중인 채팅방이 없습니다.</p>
        ) : (
          chatRooms.map((room) => (
            <div
              key={room.chatRoomId}
              className="p-4 border rounded-lg hover:bg-gray-50 dark:hover:bg-gray-800 cursor-pointer"
              onClick={() => window.location.href = `/chat/${room.chatRoomId}`}
            >
              <h2 className="font-semibold">{room.groupName}</h2>
              <p className="text-sm text-gray-500 flex items-center gap-1">
                <Users className="w-4 h-4" />
                {room.participant}
              </p>
            </div>
          ))
        )}
      </div>
    </div>
  );
}