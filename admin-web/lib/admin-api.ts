import { FirebaseError } from "firebase/app";
import { httpsCallable } from "firebase/functions";
import { getFirebaseFunctions } from "./firebase-client";
import {
  AuditLogItem,
  BetaFlags,
  DashboardStats,
  PhotoVerificationItem,
  UserSummary,
  UserEligibilitySnapshot,
} from "./types";

function callableErrorMessage(error: unknown): string {
  if (error instanceof FirebaseError) {
    if (error.code === "functions/internal") {
      return error.message || "Erro interno na Cloud Function.";
    }
    return error.message;
  }
  if (error instanceof Error) return error.message;
  return "Erro desconhecido.";
}

function fn<TRequest, TResponse>(name: string) {
  return httpsCallable<TRequest, TResponse>(getFirebaseFunctions(), name);
}

export async function getDashboardStats(): Promise<DashboardStats> {
  try {
    const call = fn<Record<string, never>, DashboardStats>("adminGetDashboardStats");
    const result = await call({});
    return result.data;
  } catch (error) {
    throw new Error(callableErrorMessage(error));
  }
}

export async function listPhotoVerifications(params: {
  status?: string;
  limit?: number;
  cursor?: string;
}): Promise<{ items: PhotoVerificationItem[]; nextCursor: string | null }> {
  try {
    const call = fn<
      typeof params,
      { items: PhotoVerificationItem[]; nextCursor: string | null }
    >("adminListPhotoVerifications");
    const result = await call(params);
    return result.data;
  } catch (error) {
    throw new Error(callableErrorMessage(error));
  }
}

export async function getUserPhotoVerification(userId: string): Promise<{
  item: PhotoVerificationItem | null;
  photoStatus: string;
}> {
  try {
    const call = fn<
      { userId: string },
      { item: PhotoVerificationItem | null; photoStatus: string }
    >("adminGetUserPhotoVerification");
    const result = await call({ userId });
    return result.data;
  } catch (error) {
    throw new Error(callableErrorMessage(error));
  }
}

export async function approvePhotoVerification(requestId: string): Promise<void> {
  try {
    const call = fn<{ requestId: string }, { ok: boolean }>("adminApprovePhotoVerification");
    await call({ requestId });
  } catch (error) {
    throw new Error(callableErrorMessage(error));
  }
}

export async function rejectPhotoVerification(
  requestId: string,
  reason: string
): Promise<void> {
  try {
    const call = fn<{ requestId: string; reason: string }, { ok: boolean }>(
      "adminRejectPhotoVerification"
    );
    await call({ requestId, reason });
  } catch (error) {
    throw new Error(callableErrorMessage(error));
  }
}

export async function searchUsers(params: {
  query?: string;
  photoStatus?: string;
  subscription?: string;
  active?: boolean;
  limit?: number;
}): Promise<{ items: UserSummary[] }> {
  try {
    const call = fn<typeof params, { items: UserSummary[] }>("adminSearchUsers");
    const result = await call(params);
    return result.data;
  } catch (error) {
    throw new Error(callableErrorMessage(error));
  }
}

export async function getUserEligibility(userId: string): Promise<UserEligibilitySnapshot> {
  try {
    const call = fn<{ userId: string }, UserEligibilitySnapshot>("adminGetUserEligibility");
    const result = await call({ userId });
    return result.data;
  } catch (error) {
    throw new Error(callableErrorMessage(error));
  }
}

export async function getUser(userId: string): Promise<Record<string, unknown>> {
  try {
    const call = fn<{ userId: string }, Record<string, unknown>>("adminGetUser");
    const result = await call({ userId });
    return result.data;
  } catch (error) {
    throw new Error(callableErrorMessage(error));
  }
}

export async function updateBetaFlags(
  userId: string,
  flags: Partial<BetaFlags>
): Promise<void> {
  try {
    const call = fn<{ userId: string; flags: Partial<BetaFlags> }, { ok: boolean }>(
      "adminUpdateBetaFlags"
    );
    await call({ userId, flags });
  } catch (error) {
    throw new Error(callableErrorMessage(error));
  }
}

export async function setAccessLevel(userId: string, accessLevel: string): Promise<void> {
  try {
    const call = fn<{ userId: string; accessLevel: string }, { ok: boolean }>(
      "adminSetAccessLevel"
    );
    await call({ userId, accessLevel });
  } catch (error) {
    throw new Error(callableErrorMessage(error));
  }
}

export async function setSubscription(userId: string, subscription: string): Promise<void> {
  try {
    const call = fn<{ userId: string; subscription: string }, { ok: boolean }>(
      "adminSetSubscription"
    );
    await call({ userId, subscription });
  } catch (error) {
    throw new Error(callableErrorMessage(error));
  }
}

export async function setUserActive(
  userId: string,
  active: boolean,
  reason?: string
): Promise<void> {
  try {
    const call = fn<{ userId: string; active: boolean; reason?: string }, { ok: boolean }>(
      "adminSetUserActive"
    );
    await call({ userId, active, reason });
  } catch (error) {
    throw new Error(callableErrorMessage(error));
  }
}

export async function adjustAiCredits(
  userId: string,
  delta: number,
  reason: string
): Promise<void> {
  try {
    const call = fn<{ userId: string; delta: number; reason: string }, { ok: boolean }>(
      "adminAdjustAiCredits"
    );
    await call({ userId, delta, reason });
  } catch (error) {
    throw new Error(callableErrorMessage(error));
  }
}

export async function listAuditLog(params: {
  limit?: number;
  cursor?: string;
  targetUserId?: string;
}): Promise<{ items: AuditLogItem[]; nextCursor: string | null }> {
  try {
    const call = fn<
      typeof params,
      { items: AuditLogItem[]; nextCursor: string | null }
    >("adminListAuditLog");
    const result = await call(params);
    return result.data;
  } catch (error) {
    throw new Error(callableErrorMessage(error));
  }
}

export interface SendNotificationResult {
  total: number;
  sent: number;
  results: { userId: string; status: "sent" | "no_token" | "error"; error?: string }[];
}

export async function sendNotification(
  userIds: string[],
  title: string,
  body: string
): Promise<SendNotificationResult> {
  try {
    const call = fn<
      { userIds: string[]; title: string; body: string },
      SendNotificationResult
    >("adminSendNotification");
    const result = await call({ userIds, title, body });
    return result.data;
  } catch (error) {
    throw new Error(callableErrorMessage(error));
  }
}
