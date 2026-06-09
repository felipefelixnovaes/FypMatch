fetch('http://localhost:3004/', {
  method: 'POST',
  headers: {
    'Next-Action': '23c250c6545bb32c90796b97e7bb641793f3ec56',
    'Content-Type': 'text/plain;charset=UTF-8',
    'Accept': 'text/x-component'
  },
  body: JSON.stringify([{"email": "e2e.real.test@example.com", "phone": "11999999955", "city": "São Paulo"}])
}).then(r => r.text()).then(console.log).catch(console.error);
