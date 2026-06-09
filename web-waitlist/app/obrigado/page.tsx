"use client";

import { useEffect, useState } from "react";
import { motion } from "framer-motion";
import { CheckCircle2, Gift, Phone } from "lucide-react";
import Link from "next/link";
import Image from "next/image";

export default function Obrigado() {
  const [referralLink, setReferralLink] = useState("");

  useEffect(() => {
    // Generate a mock referral link just for display
    const mockCode = Math.random().toString(36).substring(2, 8).toUpperCase();
    setReferralLink(`https://fypmatch.com.br/vip/${mockCode}`);
    
    // Here we'd normally fire Facebook Pixel and GA4 lead events
    // We'll implement this properly in layout.tsx soon
  }, []);

  return (
    <main className="min-h-screen flex flex-col items-center justify-center p-4 pt-24 pb-12 relative overflow-hidden">
      {/* Background decorations */}
      <div className="absolute top-[-20%] left-[-10%] w-[60%] h-[60%] bg-fypmatch-pink/20 rounded-full blur-[120px] pointer-events-none" />
      <div className="absolute bottom-[-20%] right-[-10%] w-[50%] h-[50%] bg-fypmatch-purple/20 rounded-full blur-[120px] pointer-events-none" />

      {/* Header / Logo */}
      <div className="absolute top-0 left-0 w-full p-6 flex justify-center md:justify-start items-center z-50">
        <Link href="/" className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl shadow-lg shadow-fypmatch-pink/20 overflow-hidden relative">
            <Image src="/logo.png" alt="FypMatch Logo" fill className="object-cover" />
          </div>
          <span className="font-bold text-xl tracking-tight text-white">FypMatch</span>
        </Link>
      </div>

      <motion.div 
        initial={{ opacity: 0, scale: 0.95 }}
        animate={{ opacity: 1, scale: 1 }}
        className="max-w-md w-full bg-white/5 border border-white/10 rounded-2xl p-8 backdrop-blur-xl shadow-2xl relative z-10 text-center"
      >
        <div className="w-20 h-20 bg-green-500/20 text-green-500 rounded-full flex items-center justify-center mx-auto mb-6">
          <CheckCircle2 className="w-10 h-10" />
        </div>

        <h1 className="text-3xl font-bold mb-4">Você entrou na Lista VIP do FypMatch!</h1>
        <p className="text-gray-300 mb-8">
          Em breve avisaremos quando sua cidade for liberada.
        </p>

        <div className="bg-black/40 border border-fypmatch-purple/30 rounded-xl p-6 relative overflow-hidden mb-6">
          <div className="absolute top-0 left-0 w-full h-1 bg-gradient-to-r from-fypmatch-pink to-fypmatch-purple"></div>
          
          <Gift className="w-8 h-8 text-fypmatch-purple mx-auto mb-3" />
          <h3 className="font-bold text-lg mb-2">Acelere o lançamento</h3>
          <p className="text-sm text-gray-400 mb-4">
            Convide amigos e fure a fila para ganhar o status VIP gratuito.
          </p>

          <div className="flex flex-col gap-3">
            <code className="bg-black/60 border border-white/10 rounded p-3 text-fypmatch-pink font-mono text-sm break-all">
              {referralLink}
            </code>
            <button
              onClick={() => {
                const message = encodeURIComponent(`Acabei de pegar uma das últimas vagas pro novo app de conexões reais na nossa cidade. Pega seu convite aqui para batermos a meta de lançamento mais rápido: ${referralLink}`);
                window.open(`https://wa.me/?text=${message}`, '_blank');
              }}
              className="w-full bg-[#25D366]/20 hover:bg-[#25D366]/30 text-[#25D366] border border-[#25D366]/50 py-3 rounded-lg text-sm font-medium transition-colors flex items-center justify-center gap-2"
            >
              <Phone className="w-5 h-5" />
              Compartilhar no WhatsApp
            </button>
          </div>
        </div>

        <button
          onClick={() => {
            window.open(`https://chat.whatsapp.com/ExemploGrupoVIP`, '_blank');
          }}
          className="w-full bg-white text-black hover:bg-gray-200 py-4 rounded-lg font-bold flex items-center justify-center gap-2 transition-colors"
        >
          Entre no grupo VIP no WhatsApp
        </button>
      </motion.div>
    </main>
  );
}
