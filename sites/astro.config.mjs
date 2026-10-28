import { defineConfig } from 'astro/config';
import starlight from '@astrojs/starlight';

export default defineConfig({
  site: 'https://lambdawalker.github.io',
  base: '/android.apexfission.permissions',
  integrations: [
    starlight({
      title: 'Apexfission Permissions',
      components: { Banner: './src/components/VersionBanner.astro', Sidebar: './src/components/VersionSidebar.astro', LanguageSelect: './src/components/EmptyLanguageSelect.astro', Head: './src/components/VersionHead.astro' },
      defaultLocale: 'en',
      locales: { en: { label: 'English', lang: 'en' }, es: { label: 'Español', lang: 'es' } },
      description: 'Android runtime permissions, explained with Compose.',
      favicon: '/favicon.svg',
      customCss: ['./src/styles/brand.css'],
      social: [{ icon: 'github', label: 'GitHub', href: 'https://github.com/lambdawalker/android.apexfission.permissions' }],
      sidebar: [],
    }),
  ],
});
