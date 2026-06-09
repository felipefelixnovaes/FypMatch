import Link from 'next/link';

interface PageProps {
  params: {
    referralCode: string;
  };
}

export default function IndiqueAmigos({ params }: PageProps) {
  return (
    <div className="min-h-screen bg-black text-white flex flex-col items-center justify-center p-4">
      <div className="max-w-md w-full space-y-8 text-center">
        <h1 className="text-4xl font-bold bg-gradient-to-r from-purple-400 to-pink-600 bg-clip-text text-transparent">
          Você entrou na lista VIP. Agora pode subir de posição.
        </h1>
        <p className="text-xl text-gray-300">
          Convide amigos para conhecer o FypMatch. Cada pessoa que entrar pelo seu link aumenta sua prioridade no lançamento.
        </p>
        
        <div className="bg-gray-900 p-6 rounded-2xl border border-gray-800">
          <p className="text-sm text-gray-400 mb-2">Seu link único:</p>
          <div className="bg-black p-3 rounded-lg text-pink-500 font-mono text-sm break-all">
            https://fypmatch.com/indique/{params.referralCode}
          </div>
          <button className="mt-4 w-full bg-white text-black font-bold py-3 rounded-full hover:bg-gray-200 transition-colors">
            Copiar meu link
          </button>
        </div>
      </div>
    </div>
  );
}
