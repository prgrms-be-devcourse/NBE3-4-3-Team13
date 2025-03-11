'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import KakaoMap from '@/components/groups/KakaoMap';
import { Button } from '@/components/ui/button';
import DeleteConfirmModal from '@/components/groups/DeleteConfirmModal';
import ConfirmModal from '@/components/common/ConfirmModal';

interface GroupDetail {
  id: number;
  categoryName: string;
  name: string;
  province: string;
  city: string;
  town: string;
  description: string;
  recruitStatus: string;
  maxRecruitCount: number;
  currentMemberCount: number;
  createdAt: string;
  isApplying: boolean;
  isMember: boolean;
  isAdmin: boolean;
  groupLeaders: string[];
  latitude: string;
  longitude: string;
  isLiked: boolean;
  likeCount: number;
}

interface Post {
  id: number;
  title: string;
  content: string;
  author: string;
  createdAt: string;
}

interface Props {
  groupId: string;
}

export default function ClientPage({ groupId }: Props) {
  const router = useRouter();
  const [group, setGroup] = useState<GroupDetail | null>(null);
  const [coordinates, setCoordinates] = useState<{ latitude: string; longitude: string } | null>(null);
  const [posts, setPosts] = useState<Post[]>([]);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [error, setError] = useState<string | null>(null);
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [showLeaveModal, setShowLeaveModal] = useState(false);

  console.log('GroupId received:', groupId);

  useEffect(() => {
    const fetchGroupData = async () => {
      try {
        const token = localStorage.getItem('accessToken');
        const response = await fetch(`http://localhost:8080/api/v1/groups/${groupId}`, {
          headers: {
            Accept: 'application/json',
            'Content-Type': 'application/json',
            Authorization: `Bearer ${token}`,
          },
          credentials: 'include',
        });
        const data = await response.json();
        console.log('서버에서 받은 그룹 데이터:', data);  // 전체 응답 데이터 확인
        console.log('그룹 상세 정보:', data.data);  // 실제 그룹 데이터 확인
        if (data.isSuccess) {
          setGroup(data.data);
          console.log('저장된 그룹 상태:', data.data);  // 저장되는 데이터 확인
        }
      } catch (error) {
        console.error('Error fetching group:', error);
      }
    };

    if (groupId) {
      fetchGroupData();
    }
  }, [groupId]);

  useEffect(() => {
    if (group) {
      fetchCoordinates();
    }
  }, [group]);

  const fetchCoordinates = async () => {
    if (!group) return;

    try {
      const token = localStorage.getItem('accessToken');
      const response = await fetch(
        `http://localhost:8080/api/v1/proxy/kakao/address?province=${group.province}&city=${group.city}&town=${group.town}`,
        {
          headers: {
            Accept: 'application/json',
            'Content-Type': 'application/json',
            Authorization: `Bearer ${token}`,
          },
        }
      );
      const data = await response.json();
      console.log('Raw coordinates response:', data); // 디버깅용

      if (data.isSuccess && data.data.documents.length > 0) {
        const firstResult = data.data.documents[0];
        const coordinates = {
          latitude: firstResult.y,
          longitude: firstResult.x,
        };
        console.log('Setting coordinates:', coordinates); // 디버깅용
        setCoordinates(coordinates);
      }
    } catch (error) {
      console.error('Failed to fetch coordinates:', error);
    }
  };

  const handleLeaveGroup = async () => {
    try {
      const token = localStorage.getItem('accessToken');
      if (!token) {
        setError('로그인이 필요합니다.');
        return;
      }

      if (!group) {
        setError('모임 정보를 불러올 수 없습니다.');
        return;
      }

      const response = await fetch(`http://localhost:8080/api/v1/groups/${groupId}/leave`, {
        method: 'DELETE',
        headers: {
          Accept: 'application/json',
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`,
        },
        credentials: 'include',
      });

      const data = await response.json();
      if (data.isSuccess) {
        router.push('/groups');
      } else {
        setError(
          group.isAdmin && group.groupLeaders.length === 1
            ? '모임의 유일한 관리자는 탈퇴할 수 없습니다. 다른 회원에게 관리자 권한을 위임한 후 탈퇴해주세요.'
            : '모임 탈퇴에 실패했습니다.'
        );
      }
    } catch (error) {
      console.error('Failed to leave group:', error);
      setError('모임 탈퇴 중 오류가 발생했습니다.');
    }
  };

  const handleJoinClick = () => {
    router.push(`/groups/${groupId}/join`);
  };

  const handleEditClick = () => {
    if (!group?.isAdmin) {
      setError('관리자만 모임을 수정할 수 있습니다.');
      return;
    }
    router.push(`/groups/${groupId}/edit`);
  };

  const handleDeleteClick = async () => {
    if (!group?.isAdmin) {
      setError('관리자만 모임을 삭제할 수 있습니다.');
      return;
    }

    if (!window.confirm('정말로 이 모임을 삭제하시겠습니까?')) {
      return;
    }

    try {
      const token = localStorage.getItem('accessToken');
      if (!token) {
        setError('로그인이 필요합니다.');
        return;
      }

      const response = await fetch(`http://localhost:8080/api/v1/groups/${groupId}`, {
        method: 'DELETE',
        headers: {
          Accept: 'application/json',
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`,
        },
        credentials: 'include',
      });

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || '모임 삭제에 실패했습니다.');
      }

      router.push('/groups');
    } catch (error) {
      setError(error instanceof Error ? error.message : '모임 삭제 중 오류가 발생했습니다.');
    }
  };

  const handleLikeToggle = async () => {
    try {
      const token = localStorage.getItem('accessToken');
      if (!token) {
        setError('로그인이 필요합니다.');
        return;
      }

      // 현재 상태 저장
      const currentLikeStatus = group?.isLiked;
      console.log('현재 좋아요 상태:', currentLikeStatus);  // 디버깅용

      // API 요청
      const response = await fetch(`http://localhost:8080/api/v1/groups/${groupId}/like`, {
        method: currentLikeStatus ? 'DELETE' : 'POST',
        headers: {
          Authorization: `Bearer ${token}`,
          'Content-Type': 'application/json',
        },
        credentials: 'include',
      });

      const data = await response.json();
      console.log('서버 응답:', data);  // 디버깅용

      if (data.isSuccess) {
        // API 성공시에만 상태 업데이트
        setGroup(prev => prev ? {
          ...prev,
          isLiked: !currentLikeStatus,
          likeCount: currentLikeStatus ? prev.likeCount - 1 : prev.likeCount + 1
        } : null);
      } else {
        console.log('실패 이유:', data.message);  // 디버깅용
        setError(data.message || '좋아요 처리에 실패했습니다.');
      }
    } catch (error) {
      console.error('좋아요 처리 중 오류 발생:', error);
      setError('좋아요 처리 중 오류가 발생했습니다.');
    }
  };

  if (!group) {
    return <div>Loading...</div>;
  }

  return (
    <div className='container mx-auto px-4 py-8'>
      {/* 카카오맵 */}
      {coordinates && (
        <>
          <div className='hidden'>Coordinates: {JSON.stringify(coordinates)}</div>
          <KakaoMap
            latitude={coordinates.latitude}
            longitude={coordinates.longitude}
            level={6}
            groupName={group.name}
            address={`${group.province} ${group.city} ${group.town}`}
          />
        </>
      )}

      {/* 그룹 상세 정보 */}
      <div className='bg-white dark:bg-gray-800 rounded-lg shadow-lg p-6 mb-8'>
        <div className='flex justify-between items-center mb-6'>
          <div className='flex items-center gap-4'>
            <h1 className='text-3xl font-bold text-gray-900 dark:text-white mb-2'>{group.name}</h1>
            <button
              onClick={handleLikeToggle}
              className='flex items-center gap-1 text-gray-600 hover:text-red-500 transition-colors'
            >
              {group.isLiked ? (
                <svg className="w-6 h-6 fill-red-500" viewBox="0 0 24 24">
                  <path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/>
                </svg>
              ) : (
                <svg className="w-6 h-6 stroke-current" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z"/>
                </svg>
              )}
              <span>{group.likeCount}</span>
            </button>
          </div>
          <div>
            <div className='flex items-center gap-2'>
              <span className='bg-blue-100 dark:bg-blue-900 text-blue-800 dark:text-blue-100 px-3 py-1 rounded-full text-sm font-medium'>
                {group.categoryName}
              </span>
              <span className='text-gray-600 dark:text-gray-400 text-sm'>
                생성일: {new Date(group.createdAt).toLocaleDateString()}
              </span>
            </div>
          </div>
        </div>

        <div className='space-y-4'>
          <div className='flex items-center gap-2 text-gray-700 dark:text-gray-300'>
            <span className='font-medium'>관리자:</span>
            <div className='flex flex-wrap gap-2'>
              {group.groupLeaders.map((leader, index) => (
                <span key={index} className='bg-gray-100 dark:bg-gray-700 px-2 py-1 rounded-md text-sm'>
                  {leader}
                </span>
              ))}
            </div>
          </div>
          <p>
            <span className='font-semibold'>위치:</span> {group.province} {group.city} {group.town}
          </p>
          <p>
            <span className='font-semibold'>모집 상태:</span>{' '}
            <span
              className={`px-2 py-1 rounded-full text-sm ${
                group.recruitStatus === 'RECRUITING'
                  ? 'bg-green-100 dark:bg-green-900 text-green-800 dark:text-green-100'
                  : 'bg-red-100 dark:bg-red-900 text-red-800 dark:text-red-100'
              }`}
            >
              {group.recruitStatus === 'RECRUITING' ? '모집중' : '모집완료'}
            </span>
          </p>
          <p>
            <span className='font-semibold'>멤버:</span> {group.currentMemberCount}/{group.maxRecruitCount}
          </p>
          <div>
            <span className='font-semibold'>소개:</span>
            <p className='mt-2 whitespace-pre-wrap'>{group.description}</p>
          </div>
        </div>
      </div>

      {/* 하단 버튼 섹션 */}
      <div className='flex flex-col gap-4 md:flex-row md:justify-between items-center'>
        <div className='flex gap-4'>
          {group.isMember ? (
            <>
              <button
                onClick={() => setShowLeaveModal(true)}
                className='px-4 py-2 bg-amber-600 text-white rounded-md hover:bg-amber-700 transition-colors'
              >
                모임 탈퇴
              </button>
              {group.isAdmin && (
                <>
                  <button
                    onClick={() => router.push(`/groups/${groupId}/meeting_applications`)}
                    className='px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition-colors'
                  >
                    모임 관리
                  </button>
                  <button
                    onClick={handleEditClick}
                    className='px-4 py-2 bg-indigo-600 text-white rounded-md hover:bg-indigo-700 transition-colors'
                  >
                    모임 수정
                  </button>
                  <button
                    onClick={handleDeleteClick}
                    className='px-4 py-2 bg-rose-600 text-white rounded-md hover:bg-rose-700 transition-colors'
                  >
                    모임 삭제
                  </button>
                </>
              )}
            </>
          ) : group.isApplying ? (
            <button disabled className='px-4 py-2 bg-gray-400 text-white rounded-md cursor-not-allowed'>
              가입 신청 중
            </button>
          ) : (
            <div className='relative group'>
              <button
                onClick={handleJoinClick}
                disabled={group.recruitStatus === 'CLOSED'}
                className={`px-4 py-2 rounded-md transition-colors ${
                  group.recruitStatus === 'CLOSED'
                    ? 'bg-gray-400 text-white cursor-not-allowed'
                    : 'bg-emerald-600 text-white hover:bg-emerald-700'
                }`}
              >
                모임 가입
              </button>
              {group.recruitStatus === 'CLOSED' && (
                <div className='absolute bottom-full left-1/2 transform -translate-x-1/2 mb-2 px-3 py-1 bg-gray-800 text-white text-sm rounded-md opacity-0 group-hover:opacity-100 transition-opacity whitespace-nowrap'>
                  모집 중이 아닙니다
                </div>
              )}
            </div>
          )}
        </div>

        <button
          onClick={() => router.push(`/groups/${groupId}/post`)}
          className='w-full md:w-auto px-6 py-3 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition-colors flex items-center justify-center gap-2'
        >
          <svg
            className='w-5 h-5'
            fill='none'
            stroke='currentColor'
            viewBox='0 0 24 24'
            xmlns='http://www.w3.org/2000/svg'
          >
            <path
              strokeLinecap='round'
              strokeLinejoin='round'
              strokeWidth={2}
              d='M19 20H5a2 2 0 01-2-2V6a2 2 0 012-2h10a2 2 0 012 2v1m2 13a2 2 0 01-2-2V7m2 13a2 2 0 002-2V9.5a2.5 2.5.0 00-2.5-2.5H15'
            />
          </svg>
          모임 게시판 가기
        </button>
      </div>

      {error && <div className='mt-4 p-4 bg-destructive/10 text-destructive rounded-md'>{error}</div>}

      {/* 탈퇴 확인 모달 */}
      {showLeaveModal && (
        <ConfirmModal
          title='모임 탈퇴'
          message='정말로 이 모임을 탈퇴하시겠습니까?'
          onConfirm={() => {
            handleLeaveGroup();
            setShowLeaveModal(false);
          }}
          onCancel={() => setShowLeaveModal(false)}
        />
      )}

      {/* 삭제 확인 모달 */}
      {showDeleteModal && (
        <DeleteConfirmModal onClose={() => setShowDeleteModal(false)} onConfirm={handleDeleteClick} />
      )}
    </div>
  );
}
