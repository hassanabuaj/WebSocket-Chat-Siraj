/**
 * תקציר שיחה לתצוגת רשימת "אחרונות".
 */
export interface ConversationSummary {
  otherUid: string;
  otherEmail: string | null;
  lastTimestampIso: string | null;
}
