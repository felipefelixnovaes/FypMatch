'use server';

export async function submitLeadToCRM(data: { 
  email: string; 
  phone: string; 
  city: string;
  name?: string;
  age?: string;
  gender?: string;
  lookingFor?: string;
  goal?: string;
}) {
  const nextCrmUrl = process.env.NEXTCRM_API_URL || 'http://localhost:3000';

  // Create a fallback name from the email if not provided
  const nameToUse = data.name && data.name.trim() !== "" ? data.name : data.email.split('@')[0];

  const payload = {
    name: nameToUse,
    email: data.email,
    phone: data.phone,
    lgpdConsent: true,
    source: "FypMatch Waitlist",
    campaign: "vip-waitlist-launch",
    landingPageId: "LP-003", 
    utmCampaign: "fypmatch-alfa",
    utmTerm: data.city,
    // Add custom fields for NextCRM mapping
    customFields: {
      age: data.age,
      gender: data.gender,
      lookingFor: data.lookingFor,
      goal: data.goal
    }
  };

  try {
    const response = await fetch(`${nextCrmUrl}/api/public/leads`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(payload),
      cache: 'no-store'
    });

    if (!response.ok) {
      console.error('Failed to submit lead to NextCRM:', await response.text());
      return { success: true, message: 'CRM unavailable, but continuing flow' };
    }

    return { success: true };
  } catch (error) {
    console.error('Error submitting lead to NextCRM:', error);
    return { success: true, message: 'CRM unavailable, but continuing flow' };
  }
}
