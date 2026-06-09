"use client";

import { useState, useEffect } from "react";
import Image from "next/image";
import { useRouter } from "next/navigation";
import { motion, AnimatePresence } from "framer-motion";
import {
  Users, MapPin, Sparkles, Phone, Mail, ChevronRight, CheckCircle2, Gift,
  Heart, Gamepad2, Activity, UserMinus, ShieldCheck, Lock, Info, HelpCircle, UserPlus
} from "lucide-react";

import { submitLeadToCRM } from "./actions/submitLead";

// Mock data based on requirements
const CITIES = [
  { id: "sp", name: "São Paulo", seed: 10, realTimeAdd: 14 },
  { id: "rj", name: "Rio de Janeiro", seed: 20, realTimeAdd: 12 },
  { id: "bh", name: "Belo Horizonte", seed: 30, realTimeAdd: 8 },
  { id: "jf", name: "Juiz de Fora", seed: 40, realTimeAdd: 5 },
];

const HERO_SLIDES = [
  { top: "Cansado de matches", highlight: "que não rendem?" },
  { top: "Chega de conversas", highlight: "superficiais." },
  { top: "O primeiro app que", highlight: "te entende de verdade." },
  { top: "Conexões profundas para", highlight: "pessoas reais." },
];

export default function Home() {
  const [step, setStep] = useState<"form_1" | "form_2" | "success">("form_1");
  const [selectedCity, setSelectedCity] = useState("sp");
  const [formData, setFormData] = useState({ name: "", email: "", phone: "", age: "", gender: "", lookingFor: "", goal: "" });
  const [isLoading, setIsLoading] = useState(false);
  const [referralLink, setReferralLink] = useState("");
  const [currentSlide, setCurrentSlide] = useState(0);
  const [openFaq, setOpenFaq] = useState<number | null>(null);

  const activeCity = CITIES.find((c) => c.id === selectedCity) || CITIES[0];

  // Realtime counter simulation logic
  const [recentSignups, setRecentSignups] = useState(activeCity.realTimeAdd);
  const router = useRouter();

  // Auto-rotate the hero text
  useEffect(() => {
    let mounted = true;
    const timer = setInterval(() => {
      if (mounted) {
        setCurrentSlide((prev) => (prev + 1) % HERO_SLIDES.length);
      }
    }, 3500);
    return () => {
      mounted = false;
      clearInterval(timer);
    };
  }, []);

  // Simulate people joining right now
  useEffect(() => {
    setRecentSignups(activeCity.realTimeAdd);
    const interval = setInterval(() => {
      if (Math.random() > 0.6) { // 40% chance to increment every few seconds
        setRecentSignups(prev => prev + 1);
      }
    }, 5000);
    return () => clearInterval(interval);
  }, [activeCity.id, activeCity.realTimeAdd]);

  const handleNextStep = async (e: React.FormEvent) => {
    e.preventDefault();

    // Captura parcial no passo 1 (Progressive Profiling)
    setIsLoading(true);
    try {
      await submitLeadToCRM({
        name: formData.name,
        email: formData.email,
        phone: formData.phone,
        city: selectedCity,
      });
    } catch (error) {
      console.error("Erro no passo 1:", error);
    } finally {
      setIsLoading(false);
      setStep("form_2");
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);

    // Enviar dados para o NextCRM da Fábrica Digital
    await submitLeadToCRM({
      name: formData.name,
      email: formData.email,
      phone: formData.phone,
      city: selectedCity,
      age: formData.age,
      gender: formData.gender,
      lookingFor: formData.lookingFor,
      goal: formData.goal
    });

    // Generate a mock referral link
    const mockCode = Math.random().toString(36).substring(2, 8).toUpperCase();

    // Convert routing path to /obrigado as requested
    router.push(`/obrigado?code=${mockCode}&city=${selectedCity}`);
  };

  const copyReferral = () => {
    navigator.clipboard.writeText(referralLink);
    alert("Link copiado! Compartilhe com amigos para ganhar créditos.");
  };

  const faqs = [
    { q: "Quando o app será liberado na minha cidade?", a: "O FypMatch funciona à base de densidade local para garantir que você tenha conexões reais perto de você. O app será ativado em cada cidade assim que a meta de usuários na Lista VIP for atingida." },
    { q: "Preciso pagar para entrar na lista?", a: "Não. A lista VIP é 100% gratuita." },
    { q: "Vou receber spam?", a: "Não. Você receberá apenas avisos sobre o lançamento e informações do seu acesso." },
    { q: "O FypMatch vai funcionar no meu celular?", a: "Sim! O FypMatch foi desenhado para funcionar perfeitamente em todas as plataformas móveis (Android e iOS) desde o primeiro dia de lançamento na sua cidade." },
    { q: "Como funciona o VIP gratuito?", a: "Os primeiros cadastros elegíveis poderão receber benefícios promocionais (como Premium grátis) no lançamento." }
  ];

  return (
    <main className="min-h-screen relative overflow-x-hidden flex flex-col items-center pt-16 md:pt-24">
      {/* Header / Logo */}
      <div className="absolute top-0 left-0 w-full p-4 md:p-8 flex justify-center md:justify-start items-center z-50">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 md:w-12 md:h-12 rounded-xl shadow-lg shadow-fypmatch-pink/20 overflow-hidden relative">
            <Image src="/logo.png" fill alt="FypMatch Logo" className="w-full h-full object-cover" />
          </div>
          <span className="font-bold text-xl tracking-tight text-white">FypMatch</span>
        </div>
      </div>

      {/* Background decorations for Hero */}
      <div className="absolute top-[-10%] left-[-10%] w-[50%] h-[50%] bg-fypmatch-pink/20 rounded-full blur-[120px] pointer-events-none" />
      <div className="absolute top-[20%] right-[-10%] w-[40%] h-[40%] bg-fypmatch-purple/20 rounded-full blur-[120px] pointer-events-none" />

      {/* --- SECTION 1: HERO --- */}
      <div className="w-full max-w-5xl z-10 flex flex-col md:grid md:grid-cols-2 gap-8 md:gap-16 items-center px-4 md:px-8 min-h-[85vh]">

        {/* Left Column: Copy & Social Proof */}
        <div className="flex flex-col space-y-4 md:space-y-6 text-center md:text-left order-1 w-full">
          <div className="inline-flex items-center justify-center md:justify-start space-x-2 bg-white/5 border border-white/10 rounded-full px-3 py-1.5 md:px-4 md:py-2 w-fit mx-auto md:mx-0">
            <Sparkles className="w-3.5 h-3.5 md:w-4 md:h-4 text-fypmatch-pink" />
            <span className="text-xs md:text-sm font-medium">Acesso VIP Antecipado</span>
          </div>

          {/* Animated Hero Text Slider */}
          <div className="h-[140px] sm:h-[150px] md:h-[180px] my-4 flex items-center justify-center md:justify-start relative w-full overflow-hidden">
            <AnimatePresence mode="wait">
              <motion.h1
                key={currentSlide}
                initial={{ opacity: 0, y: 15 }}
                animate={{ opacity: 1, y: 0 }}
                exit={{ opacity: 0, y: -15 }}
                transition={{ duration: 0.4 }}
                className="text-3xl sm:text-4xl md:text-6xl font-bold leading-tight tracking-tight absolute w-full top-0 md:top-auto"
              >
                {HERO_SLIDES[currentSlide].top} <br className="hidden md:block" />
                <span className="text-gradient">{HERO_SLIDES[currentSlide].highlight}</span>
              </motion.h1>
            </AnimatePresence>
          </div>

          <p className="text-sm md:text-lg text-gray-300 md:pr-8">
            O FypMatch aproxima pessoas por compatibilidade, intenção e interações reais — não só por foto e swipe.
          </p>
          <p className="text-xs md:text-sm text-fypmatch-pink font-medium">
            Entre na fila VIP da sua cidade e seja avisado quando o acesso antecipado for liberado.
          </p>

          {/* Social Proof (Desktop) */}
          <div className="hidden md:flex flex-row items-center justify-start gap-4 pt-4">
            <div className="flex -space-x-4">
              {[1, 2, 3, 4].map((i) => (
                <div key={i} className="w-10 h-10 rounded-full border-2 border-fypmatch-dark bg-gray-800 flex items-center justify-center overflow-hidden relative">
                  <Image src={`https://i.pravatar.cc/100?img=${i + activeCity.seed}`} alt="User" fill />
                </div>
              ))}
            </div>
            <div className="flex flex-col items-start text-left">
              <div className="flex items-center space-x-1 text-fypmatch-pink">
                <Users className="w-4 h-4" />
                <span className="text-sm font-bold uppercase tracking-wider">Acesso Limitado</span>
              </div>
              <span className="text-xs text-gray-400 font-semibold">Fila VIP aberta em {activeCity.name}</span>
            </div>
          </div>
        </div>

        {/* Right Column: Form Card */}
        <div className="bg-white/5 border border-white/10 rounded-2xl p-5 md:p-8 backdrop-blur-xl order-2 shadow-2xl w-full max-w-md mx-auto md:max-w-none relative">

          {/* FOMO Floating Badge */}
          <motion.div
            initial={{ opacity: 0, y: -10 }}
            animate={{ opacity: 1, y: 0 }}
            className="absolute -top-4 -right-2 md:-right-4 bg-fypmatch-pink text-white text-[10px] md:text-xs font-bold px-3 py-1.5 rounded-full shadow-lg flex items-center gap-1.5 border border-white/20"
          >
            <span className="relative flex h-2 w-2">
              <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-white opacity-75"></span>
              <span className="relative inline-flex rounded-full h-2 w-2 bg-white"></span>
            </span>
            {recentSignups} pessoas estão se cadastrando agora
          </motion.div>

          <AnimatePresence mode="wait">
            {step === "form_1" ? (
              <motion.div
                key="form_1"
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                exit={{ opacity: 0, y: -20 }}
                className="space-y-4 md:space-y-6 pt-2"
              >
                <div className="hidden md:block">
                  <h2 className="text-2xl font-bold mb-2">Reserve seu lugar (1/2)</h2>
                  <p className="text-gray-400 text-sm">Nenhum spam. Apenas a notificação de acesso.</p>
                </div>

                <form onSubmit={handleNextStep} className="space-y-3 md:space-y-4">
                  <div className="relative">
                    <MapPin className="absolute left-3 top-3 w-5 h-5 text-gray-400" />
                    <select
                      value={selectedCity}
                      onChange={(e) => setSelectedCity(e.target.value)}
                      className="w-full bg-black/50 border border-white/10 rounded-lg py-3 pl-10 pr-4 text-sm md:text-base text-white appearance-none focus:outline-none focus:border-fypmatch-pink transition-colors"
                    >
                      {CITIES.map((city) => (
                        <option key={city.id} value={city.id} className="bg-fypmatch-darker text-white">
                          {city.name}
                        </option>
                      ))}
                    </select>
                  </div>

                  <div className="relative">
                    <Users className="absolute left-3 top-3 w-5 h-5 text-gray-400" />
                    <input
                      type="text"
                      required
                      value={formData.name}
                      onChange={(e) => setFormData({...formData, name: e.target.value})}
                      placeholder="Seu nome"
                      className="w-full bg-black/50 border border-white/10 rounded-lg py-3 pl-10 pr-4 text-sm md:text-base text-white focus:outline-none focus:border-fypmatch-pink transition-colors"
                    />
                  </div>

                  <div className="relative">
                    <Mail className="absolute left-3 top-3 w-5 h-5 text-gray-400" />
                    <input
                      type="email"
                      required
                      value={formData.email}
                      onChange={(e) => setFormData({...formData, email: e.target.value})}
                      placeholder="Seu melhor e-mail"
                      className="w-full bg-black/50 border border-white/10 rounded-lg py-3 pl-10 pr-4 text-sm md:text-base text-white focus:outline-none focus:border-fypmatch-pink transition-colors"
                    />
                  </div>

                  <div className="relative">
                    <Phone className="absolute left-3 top-3 w-5 h-5 text-gray-400" />
                    <input
                      type="tel"
                      required
                      value={formData.phone}
                      onChange={(e) => setFormData({...formData, phone: e.target.value})}
                      placeholder="WhatsApp: (00) 00000-0000"
                      className="w-full bg-black/50 border border-white/10 rounded-lg py-3 pl-10 pr-4 text-sm md:text-base text-white focus:outline-none focus:border-fypmatch-pink transition-colors"
                    />
                  </div>

                  <div className="pt-2">
                    <button
                      type="submit"
                      disabled={isLoading}
                      className="w-full bg-hero-gradient hover:opacity-90 text-white font-bold py-3.5 rounded-lg flex items-center justify-center text-sm md:text-base transition-all disabled:opacity-50 shadow-lg shadow-fypmatch-pink/20"
                    >
                      {isLoading ? (
                        <span className="animate-pulse">Avançando...</span>
                      ) : (
                        <>
                          Próxima Etapa
                          <ChevronRight className="ml-2 w-5 h-5" />
                        </>
                      )}
                    </button>
                    <p className="text-[10px] md:text-xs text-center text-gray-500 mt-3 flex items-center justify-center gap-2">
                      <ShieldCheck className="w-3 h-3" /> 18+ · Sem spam · Saia quando quiser
                    </p>
                  </div>
                </form>
              </motion.div>
            ) : step === "form_2" ? (
              <motion.div
                key="form_2"
                initial={{ opacity: 0, x: 50 }}
                animate={{ opacity: 1, x: 0 }}
                exit={{ opacity: 0, x: -50 }}
                className="space-y-4 md:space-y-6 pt-2"
              >
                <div className="hidden md:block">
                  <h2 className="text-2xl font-bold mb-2">Quase lá! (2/2)</h2>
                  <p className="text-gray-400 text-sm">Ajude a melhorar seu match no lançamento.</p>
                </div>

                <form onSubmit={handleSubmit} className="space-y-3 md:space-y-4">
                  <div className="grid grid-cols-2 gap-3">
                    <div className="relative">
                      <select
                        required
                        value={formData.age}
                        onChange={(e) => setFormData({...formData, age: e.target.value})}
                        className="w-full bg-black/50 border border-white/10 rounded-lg py-3 px-4 text-sm text-white appearance-none focus:outline-none focus:border-fypmatch-pink transition-colors"
                      >
                        <option value="" disabled className="bg-fypmatch-darker text-gray-400">Idade</option>
                        <option value="18-24" className="bg-fypmatch-darker">18-24 anos</option>
                        <option value="25-34" className="bg-fypmatch-darker">25-34 anos</option>
                        <option value="35-44" className="bg-fypmatch-darker">35-44 anos</option>
                        <option value="45+" className="bg-fypmatch-darker">45+ anos</option>
                      </select>
                    </div>

                    <div className="relative">
                      <select
                        required
                        value={formData.gender}
                        onChange={(e) => setFormData({...formData, gender: e.target.value})}
                        className="w-full bg-black/50 border border-white/10 rounded-lg py-3 px-4 text-sm text-white appearance-none focus:outline-none focus:border-fypmatch-pink transition-colors"
                      >
                        <option value="" disabled className="bg-fypmatch-darker text-gray-400">Gênero</option>
                        <option value="masculino" className="bg-fypmatch-darker">Homem</option>
                        <option value="feminino" className="bg-fypmatch-darker">Mulher</option>
                        <option value="outro" className="bg-fypmatch-darker">Outro</option>
                      </select>
                    </div>
                  </div>

                  <div className="relative">
                    <select
                      required
                      value={formData.lookingFor}
                      onChange={(e) => setFormData({...formData, lookingFor: e.target.value})}
                      className="w-full bg-black/50 border border-white/10 rounded-lg py-3 px-4 text-sm text-white appearance-none focus:outline-none focus:border-fypmatch-pink transition-colors"
                    >
                      <option value="" disabled className="bg-fypmatch-darker text-gray-400">Quem você quer conhecer?</option>
                      <option value="mulheres" className="bg-fypmatch-darker">Mulheres</option>
                      <option value="homens" className="bg-fypmatch-darker">Homens</option>
                      <option value="todos" className="bg-fypmatch-darker">Todos</option>
                    </select>
                  </div>

                  <div className="relative">
                    <select
                      required
                      value={formData.goal}
                      onChange={(e) => setFormData({...formData, goal: e.target.value})}
                      className="w-full bg-black/50 border border-white/10 rounded-lg py-3 px-4 text-sm text-white appearance-none focus:outline-none focus:border-fypmatch-pink transition-colors"
                    >
                      <option value="" disabled className="bg-fypmatch-darker text-gray-400">Objetivo principal</option>
                      <option value="relacionamento" className="bg-fypmatch-darker">Relacionamento sério</option>
                      <option value="casual" className="bg-fypmatch-darker">Encontros casuais</option>
                      <option value="amizade" className="bg-fypmatch-darker">Novas amizades</option>
                      <option value="naosei" className="bg-fypmatch-darker">Ainda não sei</option>
                    </select>
                  </div>

                  <div className="pt-2 flex gap-2">
                    <button
                      type="button"
                      onClick={() => setStep("form_1")}
                      className="bg-white/5 border border-white/10 hover:bg-white/10 text-white font-bold py-3.5 px-4 rounded-lg flex items-center justify-center transition-all"
                    >
                      Voltar
                    </button>
                    <button
                      type="submit"
                      disabled={isLoading}
                      className="flex-1 bg-hero-gradient hover:opacity-90 text-white font-bold py-3.5 rounded-lg flex items-center justify-center text-sm md:text-base transition-all disabled:opacity-50 shadow-lg shadow-fypmatch-pink/20"
                    >
                      {isLoading ? (
                        <span className="animate-pulse">Finalizando...</span>
                      ) : (
                        <>
                          Entrar na Lista VIP
                          <ChevronRight className="ml-2 w-5 h-5" />
                        </>
                      )}
                    </button>
                  </div>
                </form>
              </motion.div>
            ) : (
              <motion.div
                key="success"
                initial={{ opacity: 0, scale: 0.95 }}
                animate={{ opacity: 1, scale: 1 }}
                className="space-y-4 md:space-y-6 text-center py-4"
              >
                <div className="w-14 h-14 md:w-16 md:h-16 bg-green-500/20 text-green-500 rounded-full flex items-center justify-center mx-auto mb-4">
                  <CheckCircle2 className="w-7 h-7 md:w-8 md:h-8" />
                </div>

                <h2 className="text-xl md:text-2xl font-bold">Você está na lista!</h2>
                <p className="text-sm text-gray-300">
                  Fique de olho no WhatsApp. Avisaremos assim que a meta de usuários na sua cidade for atingida.
                </p>

                <div className="bg-black/40 border border-fypmatch-purple/30 rounded-xl p-4 md:p-5 mt-4 md:mt-6 relative overflow-hidden">
                  <div className="absolute top-0 left-0 w-full h-1 bg-gradient-to-r from-fypmatch-pink to-fypmatch-purple"></div>

                  <Gift className="w-6 h-6 md:w-8 md:h-8 text-fypmatch-purple mx-auto mb-2 md:mb-3" />
                  <h3 className="font-bold text-sm md:text-base mb-1">Acelere o lançamento em {activeCity.name}</h3>
                  <p className="text-xs text-gray-400 mb-3 md:mb-4">
                    O FypMatch precisa de massa crítica para ser ativado. Convide amigos e fure a fila para ganhar o status VIP.
                  </p>

                  <div className="flex flex-col gap-2">
                    <code className="bg-black/60 border border-white/10 rounded p-2 md:p-3 text-fypmatch-pink font-mono text-xs md:text-sm break-all">
                      {referralLink}
                    </code>
                    <button
                      onClick={() => {
                        const message = encodeURIComponent(`Acabei de pegar uma das últimas vagas pro novo app de conexões reais em ${activeCity.name}. Pega seu convite aqui para batermos a meta de lançamento mais rápido: ${referralLink}`);
                        window.open(`https://wa.me/?text=${message}`, '_blank');
                      }}
                      className="w-full bg-[#25D366]/20 hover:bg-[#25D366]/30 text-[#25D366] border border-[#25D366]/50 py-2 md:py-2.5 rounded text-xs md:text-sm font-medium transition-colors flex items-center justify-center gap-2"
                    >
                      <Phone className="w-4 h-4" />
                      Compartilhar no WhatsApp
                    </button>
                  </div>
                </div>
              </motion.div>
            )}
          </AnimatePresence>
        </div>

        {/* Social Proof (Mobile Only) */}
        <div className="md:hidden flex flex-row items-center justify-center gap-3 pt-4 order-3 w-full pb-8">
          <div className="flex -space-x-3">
            {[1, 2, 3].map((i) => (
              <div key={i} className="w-8 h-8 rounded-full border-2 border-fypmatch-dark bg-gray-800 flex items-center justify-center overflow-hidden relative">
                <Image src={`https://i.pravatar.cc/100?img=${i + activeCity.seed}`} alt="User" fill />
              </div>
            ))}
          </div>
          <div className="flex flex-col items-start text-left">
            <div className="flex items-center space-x-1 text-fypmatch-pink">
              <Users className="w-3.5 h-3.5" />
              <span className="text-xs font-bold uppercase">Acesso Limitado</span>
            </div>
            <span className="text-[10px] text-gray-400 uppercase tracking-wider font-semibold">Fila VIP aberta em {activeCity.name}</span>
          </div>
        </div>
      </div>

      {/* --- SECTION 2: POR QUE É DIFERENTE? --- */}
      <div className="w-full max-w-5xl px-4 md:px-8 py-16 md:py-24 border-t border-white/5 relative z-10">
        <div className="text-center mb-12">
          <h2 className="text-2xl md:text-4xl font-bold mb-4">Por que o FypMatch é diferente?</h2>
          <p className="text-gray-400 max-w-2xl mx-auto">Feito para quem cansou do jogo de aparências e busca algo que realmente faça sentido.</p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-6 md:gap-8">
          <div className="bg-white/5 border border-white/10 rounded-2xl p-6 hover:bg-white/10 transition-colors">
            <div className="w-12 h-12 bg-fypmatch-pink/20 rounded-xl flex items-center justify-center text-fypmatch-pink mb-4">
              <Heart className="w-6 h-6" />
            </div>
            <h3 className="text-xl font-bold mb-2">Compatibilidade real</h3>
            <p className="text-gray-400 text-sm leading-relaxed">Perfis são sugeridos com base em valores, estilo de vida, personalidade e objetivo de relacionamento. Menos aleatoriedade, mais sentido.</p>
          </div>

          <div className="bg-white/5 border border-white/10 rounded-2xl p-6 hover:bg-white/10 transition-colors">
            <div className="w-12 h-12 bg-fypmatch-purple/20 rounded-xl flex items-center justify-center text-fypmatch-purple mb-4">
              <Gamepad2 className="w-6 h-6" />
            </div>
            <h3 className="text-xl font-bold mb-2">Jogos para quebrar o gelo</h3>
            <p className="text-gray-400 text-sm leading-relaxed">Não sabe o que falar? Dilemas, perguntas e desafios embutidos ajudam a conversa começar de forma natural e divertida.</p>
          </div>

          <div className="bg-white/5 border border-white/10 rounded-2xl p-6 hover:bg-white/10 transition-colors">
            <div className="w-12 h-12 bg-blue-500/20 rounded-xl flex items-center justify-center text-blue-400 mb-4">
              <Activity className="w-6 h-6" />
            </div>
            <h3 className="text-xl font-bold mb-2">Conexão Viva</h3>
            <p className="text-gray-400 text-sm leading-relaxed">Enquanto vocês conversam e interagem, um gráfico mostra sinais reais de afinidade, reciprocidade e continuidade da relação.</p>
          </div>

          <div className="bg-white/5 border border-white/10 rounded-2xl p-6 hover:bg-white/10 transition-colors">
            <div className="w-12 h-12 bg-orange-500/20 rounded-xl flex items-center justify-center text-orange-400 mb-4">
              <UserMinus className="w-6 h-6" />
            </div>
            <h3 className="text-xl font-bold mb-2">Menos match vazio</h3>
            <p className="text-gray-400 text-sm leading-relaxed">A ideia não é colecionar curtidas em uma gaveta. O app é desenhado para aumentar a chance de conversas que realmente evoluem.</p>
          </div>
        </div>
      </div>

      {/* --- SECTION 3: COMO FUNCIONA --- */}
      <div className="w-full max-w-5xl px-4 md:px-8 py-16 md:py-24 border-t border-white/5 relative z-10">
        <h2 className="text-2xl md:text-4xl font-bold mb-12 text-center">Como funciona</h2>

        <div className="flex flex-col md:flex-row gap-8">
          {[
            { step: "1", title: "Entre na lista VIP", desc: "Cadastre sua cidade e contato para reservar sua vaga." },
            { step: "2", title: "Responda seu perfil", desc: "Quando o acesso abrir, você completa seus dados no app." },
            { step: "3", title: "Conheça pessoas", desc: "O FypMatch sugere conexões com base no que combina com você." },
            { step: "4", title: "Descubra a conexão", desc: "Converse, jogue e veja a afinidade evoluir no gráfico." }
          ].map((item, index) => (
            <div key={index} className="flex-1 relative">
              <div className="hidden md:block absolute top-6 left-12 right-0 h-[2px] bg-white/10 z-0" />
              <div className="relative z-10 flex flex-col items-center text-center">
                <div className="w-12 h-12 rounded-full bg-fypmatch-dark border border-fypmatch-pink/50 flex items-center justify-center font-bold text-fypmatch-pink text-xl mb-4">
                  {item.step}
                </div>
                <h3 className="font-bold mb-2">{item.title}</h3>
                <p className="text-sm text-gray-400">{item.desc}</p>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* --- SECTION 4: INCENTIVO / VIP --- */}
      <div className="w-full max-w-4xl px-4 md:px-8 py-16 relative z-10">
        <div className="bg-gradient-to-br from-fypmatch-pink/20 to-fypmatch-purple/20 border border-white/20 rounded-3xl p-8 md:p-12 text-center relative overflow-hidden">
          <div className="absolute top-0 right-0 p-8 opacity-20">
            <Gift className="w-32 h-32" />
          </div>
          <h2 className="text-2xl md:text-4xl font-bold mb-4 relative z-10">Ganhe 1 ano de VIP gratuito</h2>
          <p className="text-gray-300 md:text-lg mb-8 max-w-2xl mx-auto relative z-10">
            O acesso será liberado por cidade. Os primeiros usuários elegíveis que entrarem na fila
            terão prioridade no lançamento e poderão receber benefícios exclusivos.
          </p>
          <button
            onClick={() => window.scrollTo({ top: 0, behavior: 'smooth' })}
            className="bg-white text-black hover:bg-gray-200 font-bold py-3.5 px-8 rounded-lg transition-colors relative z-10"
          >
            Quero meu acesso antecipado
          </button>
        </div>
      </div>

      {/* --- SECTION 5: SEGURANÇA --- */}
      <div className="w-full max-w-5xl px-4 md:px-8 py-16 md:py-24 border-t border-white/5 relative z-10">
        <div className="text-center mb-12">
          <h2 className="text-2xl md:text-4xl font-bold mb-4">Feito para conexões conscientes</h2>
          <p className="text-gray-400">A segurança e a intenção vêm em primeiro lugar.</p>
        </div>

        <div className="grid grid-cols-2 md:grid-cols-4 gap-4 md:gap-6">
          <div className="bg-black/30 rounded-xl p-4 md:p-6 text-center border border-white/5">
            <Lock className="w-8 h-8 text-gray-400 mx-auto mb-3" />
            <h4 className="font-bold text-sm">Apenas maiores de 18</h4>
          </div>
          <div className="bg-black/30 rounded-xl p-4 md:p-6 text-center border border-white/5">
            <Info className="w-8 h-8 text-gray-400 mx-auto mb-3" />
            <h4 className="font-bold text-sm">Intenção declarada</h4>
          </div>
          <div className="bg-black/30 rounded-xl p-4 md:p-6 text-center border border-white/5">
            <ShieldCheck className="w-8 h-8 text-gray-400 mx-auto mb-3" />
            <h4 className="font-bold text-sm">Privacidade respeitada</h4>
          </div>
          <div className="bg-black/30 rounded-xl p-4 md:p-6 text-center border border-white/5">
            <UserPlus className="w-8 h-8 text-gray-400 mx-auto mb-3" />
            <h4 className="font-bold text-sm">Moderação ativa</h4>
          </div>
        </div>
      </div>

      {/* --- SECTION 6: FAQ --- */}
      <div className="w-full max-w-3xl px-4 md:px-8 py-16 md:py-24 relative z-10 mb-12">
        <h2 className="text-2xl md:text-4xl font-bold mb-8 text-center flex items-center justify-center gap-3">
          <HelpCircle className="w-8 h-8 text-fypmatch-pink" /> Dúvidas frequentes
        </h2>

        <div className="space-y-4">
          {faqs.map((faq, idx) => (
            <div
              key={idx}
              className="bg-white/5 border border-white/10 rounded-xl overflow-hidden"
            >
              <button
                onClick={() => setOpenFaq(openFaq === idx ? null : idx)}
                className="w-full px-6 py-4 text-left font-bold flex justify-between items-center hover:bg-white/5 transition-colors"
              >
                {faq.q}
                <ChevronRight className={`w-5 h-5 transition-transform ${openFaq === idx ? 'rotate-90 text-fypmatch-pink' : 'text-gray-500'}`} />
              </button>
              <AnimatePresence>
                {openFaq === idx && (
                  <motion.div
                    initial={{ height: 0, opacity: 0 }}
                    animate={{ height: "auto", opacity: 1 }}
                    exit={{ height: 0, opacity: 0 }}
                    className="px-6 pb-4 text-gray-400 text-sm"
                  >
                    {faq.a}
                  </motion.div>
                )}
              </AnimatePresence>
            </div>
          ))}
        </div>
      </div>

      {/* Footer */}
      <footer className="w-full border-t border-white/10 py-8 text-center text-gray-500 text-sm relative z-10 bg-black/50">
        <p>© {new Date().getFullYear()} FypMatch. Todos os direitos reservados.</p>
      </footer>
    </main>
  );
}