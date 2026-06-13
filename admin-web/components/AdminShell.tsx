"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { LogOut, Shield, Users, ClipboardList, History, Bell } from "lucide-react";
import clsx from "clsx";
import { useAuth } from "@/lib/auth-context";

const navItems = [
  { href: "/dashboard/", label: "Dashboard", icon: Shield },
  { href: "/verifications/", label: "Verificações", icon: ClipboardList },
  { href: "/users/", label: "Usuários", icon: Users },
  { href: "/notifications/", label: "Notificações", icon: Bell },
  { href: "/audit/", label: "Audit Log", icon: History },
];

export function AdminShell({ children }: { children: React.ReactNode }) {
  const pathname = usePathname();
  const { user, staffRole, signOutUser } = useAuth();

  return (
    <div className="min-h-screen bg-fypmatch-darker text-white">
      <header className="border-b border-white/10 bg-fypmatch-dark/80 backdrop-blur">
        <div className="mx-auto flex max-w-7xl items-center justify-between px-4 py-4">
          <div>
            <p className="text-xs uppercase tracking-[0.2em] text-fypmatch-pink">
              FypMatch Admin
            </p>
            <h1 className="text-lg font-semibold">Painel de Usuários</h1>
          </div>
          <div className="flex items-center gap-4 text-sm">
            <div className="hidden text-right sm:block">
              <p>{user?.email}</p>
              <p className="text-white/50">{staffRole}</p>
            </div>
            <button
              type="button"
              onClick={() => signOutUser()}
              className="inline-flex items-center gap-2 rounded-xl border border-white/10 px-3 py-2 hover:bg-white/5"
            >
              <LogOut className="h-4 w-4" />
              Sair
            </button>
          </div>
        </div>
      </header>

      <div className="mx-auto grid max-w-7xl gap-6 px-4 py-6 lg:grid-cols-[220px_1fr]">
        <nav className="flex gap-2 overflow-x-auto lg:flex-col">
          {navItems.map(({ href, label, icon: Icon }) => {
            const active = pathname === href || pathname.startsWith(href.replace(/\/$/, ""));
            return (
              <Link
                key={href}
                href={href}
                className={clsx(
                  "inline-flex items-center gap-2 rounded-xl px-3 py-2 text-sm transition",
                  active
                    ? "bg-hero-gradient text-white"
                    : "border border-white/10 text-white/70 hover:bg-white/5"
                )}
              >
                <Icon className="h-4 w-4" />
                {label}
              </Link>
            );
          })}
        </nav>

        <main>{children}</main>
      </div>
    </div>
  );
}
