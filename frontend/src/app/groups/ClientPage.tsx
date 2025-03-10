'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import Pagination from '@/components/common/Pagination';
import AddressSearchModal from '@/components/groups/AddressSearchModal';
import CategorySelectModal from '@/components/groups/CategorySelectModal';
import KakaoMapMultiMarker from '@/components/groups/KakaoMapMultiMarker';

interface GroupListInfo {
  id: number;
  categoryName: string;
  name: string;
  province: string;
  city: string;
  town: string;
  recruitStatus: string;
  maxRecruitCount: number;
  currentMemberCount: number;
  createdAt: string;
  groupLeaders: string[];
  liked: boolean;
  likeCount: number;
}

interface SearchParams {
  categoryName: string | null;
  recruitStatus: string;
  province: string;
  city: string;
  town: string;
  keyword: string;
}

interface PageResponse<T> {
  content: T[];
  totalPages: number;
  totalElements: number;
  numberOfElements: number;
  number: number;
  hasPrevious: boolean;
  hasNext: boolean;
}

interface ApiResponse<T> {
  isSuccess: boolean;
  code: string;
  message: string;
  data: PageResponse<T>;
}

export default function ClientPage() {
  const router = useRouter();
  const [groups, setGroups] = useState<GroupListInfo[]>([]);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [showCategoryModal, setShowCategoryModal] = useState(false);
  const [searchParams, setSearchParams] = useState<SearchParams>({
    categoryName: null,
    recruitStatus: '',
    province: '',
    city: '',
    town: '',
    keyword: '',
  });
  const [useAddressSearch, setUseAddressSearch] = useState(false);
  const [selectedAddress, setSelectedAddress] = useState<{
    province?: string;
    city?: string;
    town?: string;
  }>({});
  const [showAddressModal, setShowAddressModal] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [hasNext, setHasNext] = useState(false);
  const [hasPrevious, setHasPrevious] = useState(false);
  const [totalElements, setTotalElements] = useState(0);
  const [groupLocations, setGroupLocations] = useState<
    Array<{
      id: number;
      name: string;
      latitude: string;
      longitude: string;
      address: string;
    }>
  >([]);

  useEffect(() => {
    fetchGroups();
  }, [currentPage, searchParams]);

  const fetchGroups = async () => {
    try {
      const queryParams = new URLSearchParams();
      if (searchParams.categoryName) queryParams.append('categoryName', searchParams.categoryName);
      if (searchParams.recruitStatus) queryParams.append('recruitStatus', searchParams.recruitStatus);
      if (searchParams.province) queryParams.append('province', searchParams.province);
      if (searchParams.city) queryParams.append('city', searchParams.city);
      if (searchParams.town) queryParams.append('town', searchParams.town);
      if (searchParams.keyword) queryParams.append('keyword', searchParams.keyword);
      queryParams.append('page', currentPage.toString());
      queryParams.append('size', '10');

      const token = localStorage.getItem('accessToken');
      const response = await fetch(`http://localhost:8080/api/v1/groups?${queryParams}`, {
        headers: {
          Accept: 'application/json',
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`,
        },
        credentials: 'include',
      });

      const data: ApiResponse<GroupListInfo> = await response.json();
      if (data.isSuccess) {
        const pageData = data.data;
        console.log('서버에서 받아온 그룹 데이터:', pageData.content);
        setGroups(pageData.content);
        setTotalPages(pageData.totalPages);
        setCurrentPage(pageData.number);
        setHasNext(pageData.hasNext);
        setHasPrevious(pageData.hasPrevious);
        setTotalElements(pageData.totalElements);
      }
    } catch (error) {
      console.error('Failed to fetch groups:', error);
    }
  };

  const handleGroupClick = (groupId: number) => {
    console.log('Navigating to group:', groupId);
    router.push(`/groups/${groupId}`);
  };

  const handleSearch = async () => {
    setIsLoading(true);
    try {
      const queryParams = new URLSearchParams();
      if (searchParams.categoryName) queryParams.append('categoryName', searchParams.categoryName);
      if (searchParams.recruitStatus) queryParams.append('recruitStatus', searchParams.recruitStatus);
      if (searchParams.province) queryParams.append('province', searchParams.province);
      if (searchParams.city) queryParams.append('city', searchParams.city);
      if (searchParams.town) queryParams.append('town', searchParams.town);
      if (searchParams.keyword) queryParams.append('keyword', searchParams.keyword);
      queryParams.append('page', '0');
      queryParams.append('size', '10');

      const token = localStorage.getItem('accessToken');
      const response = await fetch(`http://localhost:8080/api/v1/groups?${queryParams}`, {
        headers: {
          Accept: 'application/json',
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`,
        },
        credentials: 'include',
      });

      const data = await response.json();
      if (data.isSuccess) {
        setGroups(data.data.content);
        setTotalPages(data.data.totalPages);
      }
    } catch (error) {
      console.error('Failed to fetch groups:', error);
    } finally {
      setIsLoading(false);
    }
  };

  const handleCreateClick = () => {
    router.push('/groups/create');
  };

  const handleAddressSelect = (address: { province: string; city: string; town: string }) => {
    setSelectedAddress(address);
    setShowAddressModal(false);
    setSearchParams({
      ...searchParams,
      ...address,
    });
  };

  const handleCategorySelect = (categoryName: string) => {
    setSearchParams((prev) => ({ ...prev, categoryName }));
    setShowCategoryModal(false);
  };

  const handleCategoryClear = () => {
    setSearchParams((prev) => ({ ...prev, categoryName: null }));
  };

  const handleSearchReset = () => {
    setSearchParams({
      categoryName: null,
      recruitStatus: '',
      province: '',
      city: '',
      town: '',
      keyword: '',
    });
    setCurrentPage(0);
  };

  // 좌표 가져오기
  const fetchGroupLocations = async (groups: GroupListInfo[]) => {
    try {
      const token = localStorage.getItem('accessToken');
      console.log('Fetching coordinates for groups:', groups.length); // 디버깅용

      const locations = await Promise.all(
        groups.map(async (group) => {
          try {
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
            console.log(`Coordinates for group ${group.id}:`, data); // 디버깅용

            if (data.isSuccess && data.data.documents.length > 0) {
              const firstResult = data.data.documents[0];
              return {
                id: group.id,
                name: group.name,
                latitude: firstResult.y,
                longitude: firstResult.x,
                address: `${group.province} ${group.city} ${group.town}`,
              };
            } else {
              console.warn(`No coordinates found for group ${group.id}:`, group); // 디버깅용
              return null;
            }
          } catch (error) {
            console.error(`Error fetching coordinates for group ${group.id}:`, error);
            return null;
          }
        })
      );

      const validLocations = locations.filter((loc): loc is NonNullable<typeof loc> => loc !== null);
      console.log('Valid locations found:', validLocations.length); // 디버깅용
      setGroupLocations(validLocations);
    } catch (error) {
      console.error('Failed to fetch coordinates:', error);
    }
  };

  // useEffect에서 groups 변경 시 호출
  useEffect(() => {
    if (groups.length > 0) {
      console.log('Groups updated, fetching coordinates...'); // 디버깅용
      fetchGroupLocations(groups);
    }
  }, [groups]);

  const handleLikeToggle = async (e: React.MouseEvent, groupId: number) => {
    e.stopPropagation();
    try {
      const token = localStorage.getItem('accessToken');
      if (!token) {
        alert('로그인이 필요합니다.');
        return;
      }

      const group = groups.find(g => g.id === groupId);
      if (!group) return;

      // 즉시 UI 업데이트
      setGroups(groups.map(g => {
        if (g.id === groupId) {
          return {
            ...g,
            liked: !g.liked,
            likeCount: g.liked ? g.likeCount - 1 : g.likeCount + 1
          };
        }
        return g;
      }));

      const response = await fetch(`http://localhost:8080/api/v1/groups/${groupId}/like`, {
        method: group.liked ? 'DELETE' : 'POST',
        headers: {
          Authorization: `Bearer ${token}`,
          'Content-Type': 'application/json',
        },
        credentials: 'include',
      });

      const data = await response.json();
      console.log('좋아요 토글 응답:', data);

      if (!data.isSuccess) {
        // 실패시 원래 상태로 되돌리기
        setGroups(groups.map(g => {
          if (g.id === groupId) {
            return {
              ...g,
              liked: !g.liked,
              likeCount: g.liked ? g.likeCount - 1 : g.likeCount + 1
            };
          }
          return g;
        }));
      }
    } catch (error) {
      console.error('좋아요 처리 중 오류 발생:', error);
      // 에러 발생시 원래 상태로 되돌리기
      setGroups(groups.map(g => {
        if (g.id === groupId) {
          return {
            ...g,
            liked: !g.liked,
            likeCount: g.liked ? g.likeCount - 1 : g.likeCount + 1
          };
        }
        return g;
      }));
    }
  };

  return (
    <div className='container mx-auto px-4 py-8'>
      <div className='flex justify-between items-center mb-6'>
        <h1 className='text-2xl font-bold text-gray-900 dark:text-white'>모임 목록</h1>
        <button
          onClick={() => router.push('/groups/create')}
          className='px-4 py-2 bg-indigo-600 text-white rounded-md hover:bg-indigo-700 transition-colors'
        >
          모임 만들기
        </button>
      </div>

      {/* 검색 필터 */}
      <div className='bg-white dark:bg-gray-800 rounded-lg shadow p-6 mb-6'>
        <div className='space-y-4'>
          {/* 카테고리 선택 */}
          <div>
            <label className='block mb-2 text-sm font-medium text-gray-900 dark:text-white'>카테고리</label>
            <div className='flex gap-2'>
              <input
                type='text'
                value={searchParams.categoryName || ''}
                readOnly
                placeholder='카테고리 선택 (선택사항)'
                className='flex-1 p-2 rounded-md bg-white border border-gray-300 text-gray-900 dark:bg-gray-800 dark:border-gray-600 dark:text-white'
              />
              <button
                type='button'
                onClick={() => setShowCategoryModal(true)}
                className='px-4 py-2 bg-white text-gray-800 border border-gray-300 rounded-md hover:bg-gray-50 dark:bg-gray-800 dark:text-white dark:border-gray-600 dark:hover:bg-gray-700'
              >
                선택
              </button>
              {searchParams.categoryName && (
                <button
                  type='button'
                  onClick={handleCategoryClear}
                  className='px-4 py-2 bg-gray-200 text-gray-800 rounded-md hover:bg-gray-300 dark:bg-gray-700 dark:text-white dark:hover:bg-gray-600'
                >
                  초기화
                </button>
              )}
            </div>
          </div>

          {/* 모집 상태 선택 */}
          <div>
            <label className='block mb-2 text-sm font-medium text-gray-900 dark:text-white'>모집 상태</label>
            <select
              value={searchParams.recruitStatus}
              onChange={(e) => setSearchParams({ ...searchParams, recruitStatus: e.target.value })}
              className='w-full p-2 rounded-md bg-white border border-gray-300 text-gray-900 dark:bg-gray-800 dark:border-gray-600 dark:text-white'
            >
              <option value=''>전체</option>
              <option value='RECRUITING'>모집 중</option>
              <option value='CLOSED'>모집 완료</option>
            </select>
          </div>

          {/* 주소 선택 */}
          <div>
            <label className='block mb-2 text-sm font-medium text-gray-900 dark:text-white'>지역</label>
            <div className='flex gap-2'>
              <input
                type='text'
                value={`${searchParams.province} ${searchParams.city} ${searchParams.town}`.trim()}
                readOnly
                placeholder='지역 선택 (선택사항)'
                className='flex-1 p-2 rounded-md bg-white border border-gray-300 text-gray-900 dark:bg-gray-800 dark:border-gray-600 dark:text-white'
              />
              <button
                type='button'
                onClick={() => setShowAddressModal(true)}
                className='px-4 py-2 bg-white text-gray-800 border border-gray-300 rounded-md hover:bg-gray-50 dark:bg-gray-800 dark:text-white dark:border-gray-600 dark:hover:bg-gray-700'
              >
                선택
              </button>
              {(searchParams.province || searchParams.city || searchParams.town) && (
                <button
                  type='button'
                  onClick={() => setSearchParams((prev) => ({ ...prev, province: '', city: '', town: '' }))}
                  className='px-4 py-2 bg-gray-200 text-gray-800 rounded-md hover:bg-gray-300 dark:bg-gray-700 dark:text-white dark:hover:bg-gray-600'
                >
                  초기화
                </button>
              )}
            </div>
          </div>

          {/* 키워드 검색 */}
          <div className='flex gap-2'>
            <input
              type='text'
              value={searchParams.keyword}
              onChange={(e) => setSearchParams((prev) => ({ ...prev, keyword: e.target.value }))}
              placeholder='모임 이름으로 검색'
              className='flex-1 p-2 rounded-md bg-white border border-gray-300 text-gray-900 dark:bg-gray-800 dark:border-gray-600 dark:text-white'
            />
            <button
              onClick={handleSearch}
              disabled={isLoading}
              className='px-6 py-2 bg-indigo-600 text-white rounded-md hover:bg-indigo-700 transition-colors disabled:opacity-50'
            >
              {isLoading ? '검색중...' : '검색'}
            </button>
            <button
              onClick={handleSearchReset}
              className='px-4 py-2 bg-gray-200 text-gray-800 rounded-md hover:bg-gray-300 dark:bg-gray-700 dark:text-white dark:hover:bg-gray-600'
            >
              전체 초기화
            </button>
          </div>
        </div>
      </div>

      {/* 카카오맵 */}
      {groupLocations.length > 0 && (
        <div className='mb-6'>
          <KakaoMapMultiMarker locations={groupLocations} onMarkerClick={(groupId) => handleGroupClick(groupId)} />
        </div>
      )}

      {/* 그룹 목록 */}
      {groups.length === 0 ? (
        <div className='text-center py-8 text-gray-600 dark:text-gray-400'>모임 내역이 없습니다.</div>
      ) : (
        <div className='space-y-4'>
          {groups.map((group) => (
            <div
              key={group.id}
              onClick={() => handleGroupClick(group.id)}
              className='bg-white dark:bg-gray-800 rounded-lg shadow-md hover:shadow-lg transition-shadow duration-300 p-6 cursor-pointer'
            >
              <div className='flex justify-between items-start mb-4'>
                <span className='bg-blue-100 dark:bg-blue-900 text-blue-800 dark:text-blue-100 px-3 py-1 rounded-full text-sm font-medium'>
                  {group.categoryName}
                </span>
                <div className='flex flex-col items-end gap-2'>
                  <span className='text-gray-600 dark:text-gray-400 text-sm'>
                    생성일: {new Date(group.createdAt).toLocaleDateString()}
                  </span>
                  <button
                    onClick={(e) => handleLikeToggle(e, group.id)}
                    className='flex items-center gap-1 text-gray-600 hover:text-red-500 transition-colors'
                  >
                    {group.liked ? (
                      <svg className="w-5 h-5 fill-red-500" viewBox="0 0 24 24">
                        <path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/>
                      </svg>
                    ) : (
                      <svg className="w-5 h-5 stroke-current" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z"/>
                      </svg>
                    )}
                    <span className="text-sm">{group.likeCount}</span>
                  </button>
                </div>
              </div>

              <h3 className='text-xl font-bold text-gray-900 dark:text-white mb-2'>{group.name}</h3>
              <div className='text-sm text-gray-500 dark:text-gray-400 mb-4'>
                관리자: {group.groupLeaders.join(', ')}
              </div>

              <div className='flex justify-between items-center'>
                <div className='text-gray-600 dark:text-gray-400'>
                  <span>
                    위치: {group.province} {group.city} {group.town}
                  </span>
                </div>
                <div className='flex items-center space-x-4'>
                  <span className='text-gray-600 dark:text-gray-400'>
                    멤버: {group.currentMemberCount}/{group.maxRecruitCount}
                  </span>
                  <span
                    className={`px-3 py-1 rounded-full text-sm font-medium ${
                      group.recruitStatus === 'RECRUITING'
                        ? 'bg-green-100 dark:bg-green-900 text-green-800 dark:text-green-100'
                        : 'bg-red-100 dark:bg-red-900 text-red-800 dark:text-red-100'
                    }`}
                  >
                    {group.recruitStatus === 'RECRUITING' ? '모집중' : '모집완료'}
                  </span>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* 페이지네이션 */}
      <div className='mt-8'>
        <Pagination
          currentPage={currentPage}
          totalPages={totalPages}
          hasNext={hasNext}
          hasPrevious={hasPrevious}
          totalElements={totalElements}
          onPageChange={setCurrentPage}
        />
      </div>

      {/* 주소 검색 모달 */}
      {showAddressModal && (
        <AddressSearchModal onClose={() => setShowAddressModal(false)} onSelect={handleAddressSelect} />
      )}

      {showCategoryModal && (
        <CategorySelectModal onClose={() => setShowCategoryModal(false)} onSelect={handleCategorySelect} />
      )}
    </div>
  );
}
