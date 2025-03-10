export interface Comment {
  id: number;
  content: string;
  memberId: number;
  nickname: string;
  createdAt: string;
  replyCount: number;
  likeCount: number;
  liked: boolean;
}
