import type { Song, Playlist, UserProfile } from '@/types'

export const mockSongs: Song[] = [
  {
    id: '1',
    title: '小情歌',
    artist: '苏打绿',
    coverUrl: 'https://picsum.photos/seed/song1/300/300',
    difficulty: 2,
    key: 'C',
    chordCount: 4,
    bpm: 78,
    genre: '流行',
    chordSheet: {
      sections: [
        {
          name: '主歌',
          lines: [
            { chords: 'C              G', lyrics: '这是一首简单的小情歌' },
            { chords: 'Am             Em', lyrics: '唱着人们心肠的曲折' },
            { chords: 'F              C', lyrics: '我想我很快乐' },
            { chords: 'Dm             G', lyrics: '当有你的温热' }
          ]
        },
        {
          name: '副歌',
          lines: [
            { chords: 'C              G', lyrics: '脚边的空气转了' },
            { chords: 'Am             Em', lyrics: '从此改变了' },
            { chords: 'F              C', lyrics: '我想我很快乐' },
            { chords: 'Dm       G       C', lyrics: '当有你的温热' }
          ]
        },
        {
          name: '间奏',
          lines: [
            { chords: 'C    G    Am   Em', lyrics: '' },
            { chords: 'F    C    Dm   G', lyrics: '' }
          ]
        }
      ]
    }
  },
  {
    id: '2',
    title: '成都',
    artist: '赵雷',
    coverUrl: 'https://picsum.photos/seed/song2/300/300',
    difficulty: 3,
    key: 'C',
    chordCount: 6,
    bpm: 80,
    genre: '民谣',
    chordSheet: {
      sections: [
        {
          name: '主歌',
          lines: [
            { chords: 'C              Em', lyrics: '让我掉下眼泪的' },
            { chords: 'Am             G', lyrics: '不止昨夜的酒' },
            { chords: 'F              Em', lyrics: '让我依依不舍的' },
            { chords: 'Dm             G', lyrics: '不止你的温柔' }
          ]
        },
        {
          name: '副歌',
          lines: [
            { chords: 'C              Em', lyrics: '和我在成都的街头走一走' },
            { chords: 'Am             G', lyrics: '直到所有的灯都熄灭了也不停留' },
            { chords: 'F              Em', lyrics: '你会挽着我的衣袖' },
            { chords: 'Dm       G       C', lyrics: '我会把手揣进裤兜' }
          ]
        }
      ]
    }
  },
  {
    id: '3',
    title: '晚风',
    artist: '伍佰',
    coverUrl: 'https://picsum.photos/seed/song3/300/300',
    difficulty: 2,
    key: 'G',
    chordCount: 5,
    bpm: 76,
    genre: '摇滚',
    chordSheet: {
      sections: [
        {
          name: '主歌',
          lines: [
            { chords: 'G              D', lyrics: '晚风轻踩着云朵' },
            { chords: 'Em             C', lyrics: '晚霞在慢慢降落' },
            { chords: 'G              D', lyrics: '来不及等到你温柔' },
            { chords: 'C        D       G', lyrics: '你再也不会回头' }
          ]
        }
      ]
    }
  },
  {
    id: '4',
    title: '南山南',
    artist: '马頔',
    coverUrl: 'https://picsum.photos/seed/song4/300/300',
    difficulty: 2,
    key: 'C',
    chordCount: 4,
    bpm: 75,
    genre: '民谣',
    chordSheet: {
      sections: [
        {
          name: '主歌',
          lines: [
            { chords: 'C              G', lyrics: '你在南方的艳阳里' },
            { chords: 'Am             F', lyrics: '大雪纷飞' },
            { chords: 'C              G', lyrics: '我在北方的寒夜里' },
            { chords: 'Am       F       C', lyrics: '四季如春' }
          ]
        }
      ]
    }
  },
  {
    id: '5',
    title: '董小姐',
    artist: '宋冬野',
    coverUrl: 'https://picsum.photos/seed/song5/300/300',
    difficulty: 1,
    key: 'G',
    chordCount: 4,
    bpm: 82,
    genre: '民谣',
    chordSheet: {
      sections: [
        {
          name: '主歌',
          lines: [
            { chords: 'G              D', lyrics: '董小姐' },
            { chords: 'Em             C', lyrics: '你从未忘记你的微笑' },
            { chords: 'G              D', lyrics: '就算你和我一样' },
            { chords: 'C        D       G', lyrics: '已经成长的坚强' }
          ]
        }
      ]
    }
  }
]

// 生成更多歌曲数据（总共60首）
for (let i = 6; i <= 60; i++) {
  const keys = ['C', 'D', 'G', 'F', 'A', 'E']
  const genres = ['流行', '民谣', '摇滚']
  const artists = ['苏打绿', '赵雷', '伍佰', '马頔', '宋冬野', '陈奕迅', '周杰伦', '五月天']
  
  mockSongs.push({
    id: String(i),
    title: `歌曲 ${i}`,
    artist: artists[Math.floor(Math.random() * artists.length)],
    coverUrl: `https://picsum.photos/seed/song${i}/300/300`,
    difficulty: Math.floor(Math.random() * 5) + 1,
    key: keys[Math.floor(Math.random() * keys.length)],
    chordCount: Math.floor(Math.random() * 6) + 3,
    bpm: Math.floor(Math.random() * 40) + 60,
    genre: genres[Math.floor(Math.random() * genres.length)],
    chordSheet: {
      sections: [
        {
          name: '主歌',
          lines: [
            { chords: 'C              G', lyrics: '示例歌词第一行' },
            { chords: 'Am             F', lyrics: '示例歌词第二行' }
          ]
        }
      ]
    }
  })
}

export const mockPlaylists: Playlist[] = [
  {
    id: 'p1',
    name: '我喜欢的',
    gradient: ['#DC2626', '#7C2D12'],
    songCount: 23,
    songs: ['1', '2', '3', '4', '5', '6', '7', '8', '9', '10', '11', '12', '13', '14', '15', '16', '17', '18', '19', '20', '21', '22', '23'],
    type: 'user'
  },
  {
    id: 'p2',
    name: '民谣精选',
    gradient: ['#059669', '#064E3B'],
    songCount: 15,
    songs: ['2', '4', '5', '10', '12', '15', '18', '22', '25', '28', '32', '35', '40', '45', '50'],
    type: 'user'
  },
  {
    id: 'ai1',
    name: '1-5-6-4 走向',
    gradient: ['#7C3AED', '#4C1D95'],
    songCount: 28,
    songs: ['1', '4', '6', '8', '11', '13', '16', '19', '21', '24', '26', '29', '31', '33', '36', '38', '41', '43', '46', '48', '51', '53', '55', '57', '59', '3', '7', '9'],
    type: 'ai',
    chordProgression: '1-5-6-4'
  },
  {
    id: 'ai2',
    name: '4-5-3-6 走向',
    gradient: ['#2563EB', '#1E3A8A'],
    songCount: 12,
    songs: ['2', '3', '14', '17', '23', '27', '34', '39', '44', '49', '54', '58'],
    type: 'ai',
    chordProgression: '4-5-3-6'
  },
  {
    id: 'ai3',
    name: '1-6-4-5 走向',
    gradient: ['#EA580C', '#7C2D12'],
    songCount: 19,
    songs: ['1', '2', '5', '12', '15', '20', '25', '30', '35', '37', '42', '47', '52', '56', '60', '10', '18', '28', '38'],
    type: 'ai',
    chordProgression: '1-6-4-5'
  }
]

export const mockUser: UserProfile = {
  id: 'u1',
  username: '吉他手',
  avatar: 'https://picsum.photos/seed/user1/200/200',
  points: 1280,
  stats: {
    collected: 156,
    playlists: 8,
    practiceHours: 243
  }
}

// 当前播放列表（用于播放页面）
export const currentPlaylist = mockSongs.slice(0, 60)
