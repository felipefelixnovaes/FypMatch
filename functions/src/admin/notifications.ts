import { HttpsError, onCall } from "firebase-functions/v2/https";
import { getFirestore } from "firebase-admin/firestore";
import { getMessaging } from "firebase-admin/messaging";
import { assertStaff } from "./guard";
import { writeAuditLog } from "./audit";

const db = getFirestore();

interface SendNotificationRequest {
  userIds?: string[];
  title?: string;
  body?: string;
}

type SendStatus = "sent" | "no_token" | "error";

/**
 * Envia uma notificacao push manual (admin) para 1+ usuarios.
 *
 * Usa bloco `notification` para a notificacao aparecer na bandeja do sistema mesmo
 * com o app fechado, sem depender de codigo extra no app. Le users/{uid}.fcmToken
 * (gravado pelo app no login). Apenas ADMIN/MODERATOR. Registra no audit log.
 */
export const adminSendNotification = onCall(
  { region: "southamerica-east1", timeoutSeconds: 120, memory: "256MiB" },
  async (request) => {
    const staff = await assertStaff(request, ["ADMIN", "MODERATOR"]);

    const payload = (request.data ?? {}) as SendNotificationRequest;
    const userIds = Array.from(
      new Set(
        (payload.userIds ?? []).filter(
          (id): id is string => typeof id === "string" && id.trim().length > 0
        )
      )
    );
    const title = (payload.title ?? "").trim();
    const body = (payload.body ?? "").trim();

    if (userIds.length === 0) {
      throw new HttpsError("invalid-argument", "Selecione ao menos um usuário.");
    }
    if (!title || !body) {
      throw new HttpsError("invalid-argument", "Título e mensagem são obrigatórios.");
    }
    if (userIds.length > 500) {
      throw new HttpsError("invalid-argument", "Máximo de 500 usuários por envio.");
    }

    const messaging = getMessaging();
    const results: { userId: string; status: SendStatus; error?: string }[] = [];
    let sent = 0;

    for (const userId of userIds) {
      try {
        const snap = await db.collection("users").doc(userId).get();
        const token = snap.exists ? (snap.get("fcmToken") as string | undefined) : undefined;
        if (!token) {
          results.push({ userId, status: "no_token" });
          continue;
        }
        await messaging.send({
          token,
          notification: { title, body },
          data: { type: "admin_message", title, body },
          android: { priority: "high", notification: { channelId: "general_channel" } },
        });
        sent += 1;
        results.push({ userId, status: "sent" });
      } catch (err) {
        results.push({ userId, status: "error", error: (err as Error).message });
      }
    }

    await writeAuditLog(staff, {
      action: "admin.sendNotification",
      targetUserId: userIds.length === 1 ? userIds[0] : `multiple:${userIds.length}`,
      after: { title, sent, total: userIds.length },
      reason: body.slice(0, 280),
    });

    return { total: userIds.length, sent, results };
  }
);
