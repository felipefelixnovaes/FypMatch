import Link from 'next/link';

export default function AlmaCompativel() {
  return (
    <div className="min-h-screen bg-black text-white flex flex-col items-center justify-center p-4">
      <div className="max-w-md w-full space-y-8 text-center">
        <h1 className="text-4xl font-bold bg-gradient-to-r from-purple-400 to-pink-600 bg-clip-text text-transparent">
          Teste da Alma Compatível
        </h1>
        <p className="text-xl text-gray-300">
          Descubra que tipo de pessoa combina com sua forma de amar.
        </p>
        <p className="text-sm text-gray-500">Leva menos de 2 minutos.</p>
        
        <div className="pt-8">
          <Link href="#quiz" className="bg-gradient-to-r from-purple-600 to-pink-600 text-white font-bold py-4 px-8 rounded-full hover:opacity-90 transition-opacity inline-block w-full text-lg">
            Começar meu teste
          </Link>
        </div>
      </div>
    </div>
  );
}
