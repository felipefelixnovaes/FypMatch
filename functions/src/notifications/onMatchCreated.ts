import { onDocumentCreated } from "firebase-functions/v2/firestore";
import { getFirestore } from "firebase-admin/firestore";
import { getMessaging } from "firebase-admin/messaging";
import * as logger from "firebase-functions/logger";

const db = getFirestore();

/**
 * Dispara um push FCM quando um match e criado.
 *
 * Notifica o user2Id (quem RECEBE o match): user1Id foi quem deu o like agora e ja
 * viu o modal de match no app. Busca o fcmToken salvo em users/{uid}.fcmToken
 * (escrito pelo app no login/refresh do token). Se nao houver token, ignora.
 */
export const onMatchCreated = onDocumentCreated(
  { region: "southamerica-east1", document: "matches/{matchId}" },
  async (event) => {
    const data = event.data?.data();
    if (!data) return;

    const initiatorId = data.user1Id as string | undefined; // deu o like (viu o modal)
    const recipientId = data.user2Id as string | undefined; // recebe o push
    if (!recipientId) {
      logger.info("Match sem user2Id; nada a notificar.");
      return;
    }

    const recipientDoc = await db.collection("users").doc(recipientId).get();
    const token = recipientDoc.get("fcmToken") as string | undefined;
    if (!token) {
      logger.info(`Sem fcmToken para ${recipientId}; push de match ignorado.`);
      return;
    }

    let otherName = "alguem";
    if (initiatorId) {
      const otherDoc = await db.collection("users").doc(initiatorId).get();
      otherName =
        (otherDoc.get("displayName") as string | undefined) ||
        (otherDoc.get("profile.fullName") as string | undefined) ||
        "alguem";
    }

    try {
      await getMessaging().send({
        token,
        data: {
          type: "new_match",
          title: "Novo match! 💕",
          body: `Você deu match com ${otherName}. Que tal mandar um oi?`,
          matchId: event.params.matchId,
        },
        android: { priority: "high" },
      });
      logger.info(`Push de match enviado para ${recipientId}.`);
    } catch (err) {
      logger.error(`Falha ao enviar push de match para ${recipientId}`, err);
    }
  }
);
