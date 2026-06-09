import type { Config } from "tailwindcss";

const config: Config = {
  content: [
    "./pages/**/*.{js,ts,jsx,tsx,mdx}",
    "./components/**/*.{js,ts,jsx,tsx,mdx}",
    "./app/**/*.{js,ts,jsx,tsx,mdx}",
  ],
  theme: {
    extend: {
      colors: {
        fypmatch: {
          pink: "#FF2A7A",
          purple: "#9B30FF",
          dark: "#120B18",
          darker: "#0A0510",
        },
      },
      backgroundImage: {
        'gradient-radial': 'radial-gradient(var(--tw-gradient-stops))',
        'hero-gradient': 'linear-gradient(135deg, #FF2A7A 0%, #9B30FF 100%)',
      },
    },
  },
  plugins: [],
};
export default config;
