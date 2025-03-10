"use client";

import { useState, useEffect, useRef, use } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { usedownloadFile } from "@/app/groups/[groupId]/post/hooks/useFileDownload";
import { LoginMemberContext } from "@/stores/auth/LoginMember";
import { MoreVertical, Edit, Trash2 } from "lucide-react";
import { Post } from "@/types/Post";
import { deletePost, likePost } from "@/api/post/postapi";

interface PostContentProps {
  post: Post;
  onPostUpdated?: (updatedPost: Post) => void;
}

function PostContent({ post, onPostUpdated }: PostContentProps) {
  const [showActions, setShowActions] = useState(false);
  const dropdownRef = useRef<HTMLDivElement>(null);
  const { handleDownload } = usedownloadFile();
  const { loginMember } = use(LoginMemberContext);
  const token = localStorage.getItem("accessToken") || "";
  const router = useRouter();

  // 좋아요 상태 관리
  const [liked, setLiked] = useState(post.liked || false);
  const [likeCount, setLikeCount] = useState(post.likeCount || 0);

  // 좋아요 토글 함수
  const handleLikeToggle = async () => {
    try {
      // 좋아요 상태 임시 업데이트 (UI 즉시 반영)
      const newLiked = !liked;
      setLiked(newLiked);
      setLikeCount((prevCount: number) => newLiked ? prevCount + 1 : prevCount - 1);

      // 서버에 좋아요 상태 변경 요청
      const updatedPost = await likePost(post.postId, token);
      
      // 부모 컴포넌트에 업데이트 알림
      if (onPostUpdated) {
        onPostUpdated(updatedPost);
      }
    } catch (error) {
      console.error("게시글 좋아요 처리 중 오류:", error);
      // 오류 발생 시 원래 상태로 복원
      setLiked(!liked);
      setLikeCount((prev: number) => liked ? prev + 1 : prev - 1);
    }
  };

  // 드롭다운 닫기
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (
        dropdownRef.current &&
        !dropdownRef.current.contains(event.target as Node)
      ) {
        setShowActions(false);
      }
    };
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, [dropdownRef]);

  // 게시글 삭제
  const handleDeletePost = async () => {
    if (confirm("정말 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.")) {
      try {
        if (!token) {
          router.push("/");
          return;
        }
        await deletePost(post.postId, token);
        alert("게시글이 삭제되었습니다.");
        router.push(`/groups/${post.groupId}/post`);
      } catch (error) {
        alert("게시글 삭제에 실패했습니다.");
      }
    }
  };

  return (
    <div className="max-w-4xl mx-auto p-6 bg-white shadow rounded-lg relative">
      {/* 제목 */}
      <div className="mb-6">
        <h1 className="text-3xl font-bold mb-1">{post.title}</h1>
        <div className="text-gray-600 text-sm">
          <span>작성자: {post.nickName}</span>
          <span className="mx-1">|</span>
          <span>작성일: {post.createdAt}</span>
          <span className="mx-1">|</span>
          <span>수정일: {post.modifiedAt}</span>
        </div>
      </div>

      <hr className="border-t border-gray-300 mb-6" />

      {/* 본문 내용 및 이미지 */}
      <div className="mb-6">
        <p className="text-lg leading-relaxed whitespace-pre-line">
          {post.content}
        </p>
        {post.images && post.images.length > 0 && (
          <div className="mt-4 flex flex-col items-center gap-4">
            {post.images.map((image: any) => (
              <img
                key={image.attachmentId}
                src={image.filePath}
                alt={image.fileName}
                className="w-2/5 max-w-2xl h-auto rounded object-contain bg-gray-100"
              />
            ))}
          </div>
        )}
      </div>

      {/* 첨부파일 리스트 */}
      {post.documents && post.documents.length > 0 && (
        <div className="mb-6">
          <h2 className="text-xl font-semibold mb-3 border-b pb-2 text-gray-800">
            첨부 파일
          </h2>
          <ul className="divide-y divide-gray-200">
            {post.documents.map((doc: any) => (
              <li
                key={doc.attachmentId}
                className="px-4 py-2 flex justify-between items-center hover:bg-gray-50 cursor-pointer transition"
                onClick={() =>
                  handleDownload(doc.attachmentId, doc.fileName, token)
                }
              >
                <span className="text-sm text-gray-900 truncate">
                  ※ {doc.fileName}
                </span>
                <span className="text-xs text-gray-500">
                  {(doc.fileSize / 1024).toFixed(1)} KB
                </span>
              </li>
            ))}
          </ul>
        </div>
      )}

      {/* 게시글 드롭다운 */}
      {post.memberId === loginMember.id && (
        <div ref={dropdownRef} className="absolute top-3 right-3">
          <button
            onClick={() => setShowActions((prev) => !prev)}
            className="p-2 rounded-full hover:bg-gray-200 focus:outline-none"
          >
            <MoreVertical className="w-5 h-5 text-gray-700" />
          </button>
          {showActions && (
            <div className="absolute right-0 mt-2 w-40 bg-white border border-gray-200 shadow-lg rounded-md z-10">
              <Link
                href={`/groups/${post.groupId}/post/${post.postId}/edit`}
                legacyBehavior
              >
                <a className="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100 flex items-center gap-2">
                  <Edit className="w-4 h-4" />
                  수정하기
                </a>
              </Link>
              <button
                onClick={handleDeletePost}
                className="w-full text-left block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100 flex items-center gap-2"
              >
                <Trash2 className="w-4 h-4" />
                삭제하기
              </button>
            </div>
          )}
        </div>
      )}

      {/* 좋아요 버튼 */}
      <div className="mt-6 flex items-center space-x-4">
        <button
          onClick={handleLikeToggle}
          className="flex items-center space-x-1 text-sm hover:text-red-500 transition-colors"
        >
          <span>{liked ? "❤️" : "🤍"}</span>
          <span>{likeCount}</span>
        </button>
      </div>
    </div>
  );
}

export default PostContent;
