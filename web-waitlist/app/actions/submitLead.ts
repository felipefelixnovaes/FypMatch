'use server';

export async function submitLeadToCRM(data: { email: string; phone: string; city: string }) {
  const nextCrmUrl = process.env.NEXTCRM_API_URL || 'http://localhost:3000';

  // Create a fallback name from the email (e.g., "john.doe" from "john.doe@example.com")
  const fallbackName = data.email.split('@')[0];

  const payload = {
    name: fallbackName,
    email: data.email,
    phone: data.phone,
    lgpdConsent: true, // Assuming implicit consent for waitlist
    source: "FypMatch Waitlist",
    campaign: "vip-waitlist-launch",
    landingPageId: "LP-003", // Referencing the LP ID in Fabrica Digital
    utmCampaign: "fypmatch-alfa",
    utmTerm: data.city // Storing the city here
  };

  try {
    const response = await fetch(`${nextCrmUrl}/api/public/leads`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(payload),
      // Prevent Next.js from aggressively caching this POST request
      cache: 'no-store'
    });

    if (!response.ok) {
      console.error('Failed to submit lead to NextCRM:', await response.text());
      // We return success anyway so we don't block the user experience if CRM is temporarily down
      return { success: true, message: 'CRM unavailable, but continuing flow' };
    }

    return { success: true };
  } catch (error) {
    console.error('Error submitting lead to NextCRM:', error);
    return { success: true, message: 'CRM unavailable, but continuing flow' };
  }
}