package se.banksimulatorn.app

/** Switches for features that are built but not ready to show. */
object FeatureFlags {
    /**
     * The Gemini Nano features (AI onboarding and the "AI Sim" chat) are hidden until
     * they work reliably. With this off the model is never downloaded, the AI tab is
     * gone and onboarding starts from the default accounts.
     */
    const val AI_ENABLED = false
}
