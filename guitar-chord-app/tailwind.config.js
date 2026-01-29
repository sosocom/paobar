/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{vue,js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        primary: {
          DEFAULT: '#DC2626',
          hover: '#B91C1C',
        },
        background: {
          page: '#0A0A0A',
          card: '#18181B',
          overlay: '#09090B',
        },
        text: {
          primary: '#FAFAFA',
          secondary: '#71717A',
        }
      },
      backdropBlur: {
        xs: '2px',
      }
    },
  },
  plugins: [],
}
