package com.ideiassertiva.FypMatch.model

enum class OnboardingFeature {
    DISCOVERY,
    PROFILE,
    FILTERS_AND_TRAVEL,
    COMPATIBILITY_MAP,
    QUESTIONNAIRES,
    MATCH_CHAT,
    AI_STORE,
    PRIVACY_SAFETY,
    SETTINGS_GUIDE
}

enum class OnboardingPreviewKind {
    DISCOVERY_CARD,
    PROFILE_EDITOR,
    FILTERS_PANEL,
    COMPATIBILITY_MAP,
    QUESTIONNAIRE_FLOW,
    CHAT_THREAD,
    STORE_WALLET,
    PRIVACY_CENTER,
    SETTINGS_GUIDE
}

data class OnboardingTutorialPage(
    val feature: OnboardingFeature,
    val title: String,
    val body: String,
    val highlight: String,
    val previewKind: OnboardingPreviewKind,
    val callouts: List<String>
)

object OnboardingTutorial {
    const val Version = 2

    val pages: List<OnboardingTutorialPage> = listOf(
        OnboardingTutorialPage(
            feature = OnboardingFeature.DISCOVERY,
            title = "Descubra pessoas com intenção",
            body = "Veja cards reais, abra detalhes antes de decidir e use curtidas, passar ou supercurtidas sem pressa.",
            highlight = "Cards e descoberta",
            previewKind = OnboardingPreviewKind.DISCOVERY_CARD,
            callouts = listOf("Toque no card para ver detalhes", "Use filtros antes de sair curtindo", "Supercurtida destaca seu interesse")
        ),
        OnboardingTutorialPage(
            feature = OnboardingFeature.PROFILE,
            title = "Complete seu perfil com cuidado",
            body = "Fotos, cidade, bio e preferências ajudam o app a mostrar pessoas mais coerentes com você.",
            highlight = "Perfil e verificação",
            previewKind = OnboardingPreviewKind.PROFILE_EDITOR,
            callouts = listOf("Adicione fotos reais", "Revise cidade e intenção", "Verificação aumenta confiança")
        ),
        OnboardingTutorialPage(
            feature = OnboardingFeature.FILTERS_AND_TRAVEL,
            title = "Ajuste quem aparece para você",
            body = "Use filtros de idade, distância, estilo de vida e cidade. No modo viagem, você explora outra região antes de chegar.",
            highlight = "Filtros, cidade e modo viagem",
            previewKind = OnboardingPreviewKind.FILTERS_PANEL,
            callouts = listOf("Escolha idade e distância", "Digite ou use localização atual", "Modo viagem muda a cidade base")
        ),
        OnboardingTutorialPage(
            feature = OnboardingFeature.COMPATIBILITY_MAP,
            title = "Entenda o mapa relacional",
            body = "Questionários e sinais consentidos alimentam o mapa de compatibilidade para revelar afinidades bronze, prata e ouro.",
            highlight = "Compatibilidade explicável",
            previewKind = OnboardingPreviewKind.COMPATIBILITY_MAP,
            callouts = listOf("Veja afinidade geral", "Abra pontos fortes", "Compare sinais antes do chat")
        ),
        OnboardingTutorialPage(
            feature = OnboardingFeature.QUESTIONNAIRES,
            title = "Questionários melhoram seus matches",
            body = "Modo rápido, profundo, valores e disponibilidade deixam o mapa mais fiel à sua rotina e ao seu jeito de se vincular.",
            highlight = "Autoconhecimento guiado",
            previewKind = OnboardingPreviewKind.QUESTIONNAIRE_FLOW,
            callouts = listOf("Responda no seu tempo", "Valores podem ter privacidade", "Disponibilidade ajuda a alinhar rotina")
        ),
        OnboardingTutorialPage(
            feature = OnboardingFeature.MATCH_CHAT,
            title = "Depois do match, converse melhor",
            body = "Toque em um match para abrir o chat real. Missões e dilemas ajudam os dois a se conhecerem com leveza.",
            highlight = "Chat, missões e dilemas",
            previewKind = OnboardingPreviewKind.CHAT_THREAD,
            callouts = listOf("Matches ficam salvos", "Missões puxam assunto", "Compatibilidade abre pelo chat")
        ),
        OnboardingTutorialPage(
            feature = OnboardingFeature.AI_STORE,
            title = "Use créditos quando fizer sentido",
            body = "A lojinha concentra créditos IA, supercurtidas e impulsionamento. Recompensas só contam quando a ação termina.",
            highlight = "IA, loja e impulsos",
            previewKind = OnboardingPreviewKind.STORE_WALLET,
            callouts = listOf("Créditos IA para conselhos", "Supercurtidas para destacar", "Impulsionamento melhora visibilidade")
        ),
        OnboardingTutorialPage(
            feature = OnboardingFeature.PRIVACY_SAFETY,
            title = "Privacidade vem antes da curiosidade",
            body = "Você controla dados sensíveis, verificação de foto e limites. Denunciar ou bloquear está sempre disponível.",
            highlight = "Segurança e consentimento",
            previewKind = OnboardingPreviewKind.PRIVACY_CENTER,
            callouts = listOf("Controle o que aparece", "Foto verificada só após revisão", "Bloqueio e denúncia ficam acessíveis")
        ),
        OnboardingTutorialPage(
            feature = OnboardingFeature.SETTINGS_GUIDE,
            title = "Volte ao guia quando quiser",
            body = "A área Configurações reúne perfil, filtros, mapa, loja, privacidade e este guia para você se localizar depois.",
            highlight = "Guia dentro do app",
            previewKind = OnboardingPreviewKind.SETTINGS_GUIDE,
            callouts = listOf("Abra em Configurações", "Use como mapa do app", "Revise novidades do beta")
        )
    )

    fun isLastPage(index: Int): Boolean = index >= pages.lastIndex

    fun primaryActionLabel(index: Int): String =
        if (isLastPage(index)) "Começar" else "Próximo"
}
