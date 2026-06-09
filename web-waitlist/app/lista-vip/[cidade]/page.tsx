import Link from 'next/link';

interface PageProps {
  params: {
    cidade: string;
  };
}

export default function ListaVipCidade({ params }: PageProps) {
  const cidadeFormatada = params.cidade
    .split('-')
    .map(word => word.charAt(0).toUpperCase() + word.slice(1))
    .join(' ');

  return (
    <div className="min-h-screen bg-black text-white flex flex-col items-center justify-center p-4">
      <div className="max-w-md w-full space-y-8 text-center">
        <h1 className="text-4xl font-bold bg-gradient-to-r from-purple-400 to-pink-600 bg-clip-text text-transparent">
          O FypMatch está chegando em {cidadeFormatada}.
        </h1>
        <p className="text-xl text-gray-300">
          Entre na lista VIP e esteja entre os primeiros a conhecer pessoas compatíveis na sua região.
        </p>
        
        <div className="pt-8">
          <Link href="#form" className="bg-gradient-to-r from-purple-600 to-pink-600 text-white font-bold py-4 px-8 rounded-full hover:opacity-90 transition-opacity inline-block w-full text-lg">
            Entrar na lista VIP de {cidadeFormatada}
          </Link>
        </div>
      </div>
    </div>
  );
}
