import Link from 'next/link';

interface PageProps {
  params: {
    id: string;
  };
}

export default function Resultado({ params }: PageProps) {
  // Mock data for initial render
  const result = {
    profile: "Intensidade Leal",
    phrase: "Você não busca qualquer conexão. Você busca presença, clareza e reciprocidade.",
    compatible: "Pessoas emocionalmente disponíveis e claras",
  };

  return (
    <div className="min-h-screen bg-black text-white flex flex-col items-center p-4 py-12">
      <div className="max-w-md w-full space-y-8">
        <div className="text-center">
          <p className="text-pink-500 font-semibold mb-2">Seu resultado está pronto</p>
          <h1 className="text-4xl font-bold bg-gradient-to-r from-purple-400 to-pink-600 bg-clip-text text-transparent">
            {result.profile}
          </h1>
        </div>

        <div className="bg-gray-900 rounded-3xl p-8 border border-gray-800 text-center space-y-6">
          <p className="text-xl italic font-light text-gray-300">
            &quot;{result.phrase}&quot;
          </p>
        </div>

        <div className="space-y-4">
          <h2 className="text-2xl font-bold">Sua alma compatível</h2>
          <p className="text-gray-400">
            Você tende a combinar com pessoas que são {result.compatible.toLowerCase()}.
          </p>
        </div>

        <div className="pt-8 space-y-4">
          <Link href="/lista-vip" className="bg-gradient-to-r from-purple-600 to-pink-600 text-white font-bold py-4 px-8 rounded-full hover:opacity-90 transition-opacity flex justify-center w-full text-lg">
            Entrar na lista VIP do FypMatch
          </Link>
          <button className="w-full bg-gray-900 text-white font-bold py-4 px-8 rounded-full border border-gray-700 hover:bg-gray-800 transition-colors">
            Compartilhar meu resultado
          </button>
        </div>
      </div>
    </div>
  );
}
