import type { Metadata } from "next";
import { Inter } from "next/font/google";
import "./globals.css";
import TrackingScripts from "./components/TrackingScripts";

const inter = Inter({ subsets: ["latin"] });

export const metadata: Metadata = {
  title: "FypMatch - Apenas VIPs | Lista de Espera",
  description: "Garanta seu lugar na versão mais exclusiva do FypMatch. Convide amigos e ganhe créditos de IA.",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="pt-BR">
      <body className={`${inter.className} bg-fypmatch-dark text-white antialiased`}>
        <TrackingScripts />
        {children}
      </body>
    </html>
  );
}
