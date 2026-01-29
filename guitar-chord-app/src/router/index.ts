import { createRouter, createWebHistory } from 'vue-router'
import Library from '@/views/Library.vue'
import Playlists from '@/views/Playlists.vue'
import PlaylistDetail from '@/views/PlaylistDetail.vue'
import NowPlaying from '@/views/NowPlaying.vue'
import Profile from '@/views/Profile.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      redirect: '/library'
    },
    {
      path: '/library',
      name: 'Library',
      component: Library
    },
    {
      path: '/playlists',
      name: 'Playlists',
      component: Playlists
    },
    {
      path: '/playlist/:id',
      name: 'PlaylistDetail',
      component: PlaylistDetail
    },
    {
      path: '/now-playing',
      name: 'NowPlaying',
      component: NowPlaying
    },
    {
      path: '/profile',
      name: 'Profile',
      component: Profile
    }
  ]
})

export default router
