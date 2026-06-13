import { initializeApp } from "firebase-admin/app";
import { setGlobalOptions } from "firebase-functions/v2";

initializeApp();

setGlobalOptions({ maxInstances: 10 });

export {
  adminListPhotoVerifications,
  adminGetUserPhotoVerification,
  adminApprovePhotoVerification,
  adminRejectPhotoVerification,
  adminGetDashboardStats,
} from "./admin/photoVerification";

export {
  adminUpdateBetaFlags,
  adminSetAccessLevel,
  adminSetSubscription,
} from "./admin/userAccess";

export {
  adminSearchUsers,
  adminGetUser,
  adminSetUserActive,
  adminAdjustAiCredits,
  adminListAuditLog,
} from "./admin/support";

export { adminGetUserEligibility } from "./admin/eligibility";

export { adminSendNotification } from "./admin/notifications";

export { counselorChat } from "./counselor/chat";

export { onMatchCreated } from "./notifications/onMatchCreated";
