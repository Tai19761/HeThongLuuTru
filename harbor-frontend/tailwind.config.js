/** @type {import('tailwindcss').Config} */
export default {
  content: ["./index.html", "./src/**/*.{js,ts,jsx,tsx}"],
  theme: {
    extend: {
      colors: {
        "harbor-blue": "#1A73E8",
        "harbor-bg": "#F8FAFC",
      },
    },
  },
  plugins: [],
};
