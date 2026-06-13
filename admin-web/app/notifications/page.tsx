"use client";

import { FormEvent, useState } from "react";
import { Bell, Check, X } from "lucide-react";
import clsx from "clsx";
import { AdminGuard } from "@/components/AdminGuard";
import { ErrorBlock, LoadingBlock } from "@/components/ui";
import { searchUsers, sendNotification, SendNotificationResult } from "@/lib/admin-api";
import { UserSummary } from "@/lib/types";

export default function NotificationsPage() {
  return (
    <AdminGuard>
      <NotificationsContent />
    </AdminGuard>
  );
}

function NotificationsContent() {
  const [query, setQuery] = useState("");
  const [results, setResults] = useState<UserSummary[]>([]);
  const [searching, setSearching] = useState(false);
  const [selected, setSelected] = useState<UserSummary[]>([]);
  const [title, setTitle] = useState("");
  const [body, setBody] = useState("");
  const [sending, setSending] = useState(false);
  const [error, setError] = useState("");
  const [result, setResult] = useState<SendNotificationResult | null>(null);

  const search = async (event?: FormEvent) => {
    event?.preventDefault();
    setSearching(true);
    setError("");
    try {
      const r = await searchUsers({ query: query.trim() || undefined, limit: 30 });
      setResults(r.items);
    } catch (err) {
      setError((err as Error).message || "Erro ao buscar usuários.");
    } finally {
      setSearching(false);
    }
  };

  const toggle = (u: UserSummary) => {
    setSelected((prev) =>
      prev.some((s) => s.id === u.id) ? prev.filter((s) => s.id !== u.id) : [...prev, u]
    );
  };

  const send = async () => {
    setError("");
    setResult(null);
    if (selected.length === 0) {
      setError("Selecione ao menos um usuário.");
      return;
    }
    if (!title.trim() || !body.trim()) {
      setError("Preencha título e mensagem.");
      return;
    }
    setSending(true);
    try {
      const r = await sendNotification(
        selected.map((s) => s.id),
        title.trim(),
        body.trim()
      );
      setResult(r);
    } catch (err) {
      setError((err as Error).message || "Erro ao enviar notificação.");
    } finally {
      setSending(false);
    }
  };

  const noToken = result?.results.filter((r) => r.status === "no_token").length ?? 0;
  const failed = result?.results.filter((r) => r.status === "error").length ?? 0;

  return (
    <div className="space-y-6">
      <div>
        <h2 className="flex items-center gap-2 text-2xl font-semibold">
          <Bell className="h-5 w-5 text-fypmatch-pink" /> Notificações
        </h2>
        <p className="text-sm text-white/60">
          Envie um push manual para um ou mais usuários. (Quem não tiver o app atualizado/logado
          aparece como &quot;sem token&quot;.)
        </p>
      </div>

      {/* Composer */}
      <div className="grid gap-3 rounded-2xl border border-white/10 bg-white/5 p-4">
        <input
          value={title}
          onChange={(e) => setTitle(e.target.value)}
          maxLength={80}
          placeholder="Título da notificação"
          className="rounded-xl border border-white/10 bg-black/20 px-3 py-2 text-sm"
        />
        <textarea
          value={body}
          onChange={(e) => setBody(e.target.value)}
          maxLength={300}
          rows={3}
          placeholder="Mensagem"
          className="rounded-xl border border-white/10 bg-black/20 px-3 py-2 text-sm"
        />

        {selected.length > 0 ? (
          <div className="flex flex-wrap gap-2">
            {selected.map((u) => (
              <span
                key={u.id}
                className="inline-flex items-center gap-1 rounded-full bg-fypmatch-pink/20 px-3 py-1 text-xs"
              >
                {u.displayName || u.email}
                <button type="button" onClick={() => toggle(u)} aria-label="Remover">
                  <X className="h-3 w-3" />
                </button>
              </span>
            ))}
          </div>
        ) : (
          <p className="text-xs text-white/40">Nenhum destinatário selecionado (busque abaixo).</p>
        )}

        <button
          type="button"
          onClick={send}
          disabled={sending}
          className="rounded-xl bg-hero-gradient px-4 py-2 text-sm font-medium disabled:opacity-50 md:max-w-xs"
        >
          {sending ? "Enviando..." : `Enviar para ${selected.length} usuário(s)`}
        </button>
      </div>

      {error ? <ErrorBlock message={error} /> : null}

      {result ? (
        <div className="rounded-2xl border border-emerald-400/30 bg-emerald-400/10 p-4 text-sm">
          <p className="font-medium">
            Enviadas: {result.sent}/{result.total}
          </p>
          {noToken > 0 ? (
            <p className="text-white/70">Sem token (app desatualizado/não logado): {noToken}</p>
          ) : null}
          {failed > 0 ? <p className="text-red-300">Falhas: {failed}</p> : null}
        </div>
      ) : null}

      {/* User picker */}
      <form
        onSubmit={search}
        className="flex gap-3 rounded-2xl border border-white/10 bg-white/5 p-4"
      >
        <input
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          placeholder="Buscar usuário (e-mail, nome ou UID)"
          className="flex-1 rounded-xl border border-white/10 bg-black/20 px-3 py-2 text-sm"
        />
        <button type="submit" className="rounded-xl bg-hero-gradient px-4 py-2 text-sm font-medium">
          Buscar
        </button>
      </form>

      {searching ? <LoadingBlock /> : null}

      <div className="space-y-2">
        {results.map((u) => {
          const isSel = selected.some((s) => s.id === u.id);
          return (
            <button
              key={u.id}
              type="button"
              onClick={() => toggle(u)}
              className={clsx(
                "flex w-full items-center gap-3 rounded-2xl border p-3 text-left transition",
                isSel
                  ? "border-fypmatch-pink bg-fypmatch-pink/10"
                  : "border-white/10 bg-white/5 hover:bg-white/10"
              )}
            >
              <span
                className={clsx(
                  "flex h-6 w-6 items-center justify-center rounded-md border",
                  isSel ? "border-fypmatch-pink bg-fypmatch-pink" : "border-white/30"
                )}
              >
                {isSel ? <Check className="h-4 w-4" /> : null}
              </span>
              <span className="min-w-0 flex-1">
                <span className="block truncate font-medium">{u.displayName || u.email}</span>
                <span className="block truncate text-xs text-white/50">
                  {u.email} · {u.accessLevel}
                </span>
              </span>
            </button>
          );
        })}
      </div>
    </div>
  );
}
