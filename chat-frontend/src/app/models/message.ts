/**
 * מודל הודעת צ'אט.
 */
export interface Message {
  id?: string;
  senderId: string;
  receiverId: string;
  timestamp: string; // ISO string
  message: string;
}
