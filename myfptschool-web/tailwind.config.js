/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  theme: {
    extend: {
      fontFamily: {
        sans: ['Inter', 'sans-serif'],
      },
      colors: {
        brand: {
          orange: '#F37021',
          blue: '#0078D7',
          green: '#00A651',
        },
        status: {
          warning: '#F59E0B',
          danger: '#EF4444',
          info: '#3B82F6',
        },
        surface: {
          bg: '#F8FAFC',
          card: '#FFFFFF',
          elevated: '#F1F5F9',
        },
        border: {
          light: '#E2E8F0',
          medium: '#CBD5E1',
        },
        text: {
          primary: '#0F172A',
          secondary: '#64748B',
          tertiary: '#94A3B8',
        },
      },
    },
  },
  plugins: [],
}
