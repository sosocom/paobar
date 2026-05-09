<template>
  <!-- 固定像素尺寸的 SVG 和弦图：
       -  标准调弦 6 弦，展示 4 个品格
       -  X = 不弹，O = 空弦，实心圆点 = 按弦位，圆角矩形 = 横按
       -  非开放把位时右侧显示起始品数（如 "3fr"）
       -  顶部显示和弦名；查不到指法时只显示居中大号和弦名作为占位 -->
  <svg
    class="chord-diagram-svg"
    :width="RENDER_W"
    :height="RENDER_H"
    :viewBox="`0 0 ${W} ${H}`"
    xmlns="http://www.w3.org/2000/svg"
    aria-hidden="true"
  >
    <!-- 顶部和弦名 -->
    <text :x="W / 2" :y="10" text-anchor="middle" class="diagram-name">
      {{ displayName }}
    </text>

    <template v-if="voicing">
      <!-- baseFret > 1 时右侧的品位指示 -->
      <text
        v-if="(voicing.baseFret ?? 1) > 1"
        :x="W - 1"
        :y="fretTopY + 8"
        text-anchor="end"
        class="diagram-fretlabel"
      >{{ voicing.baseFret }}fr</text>

      <!-- 六根弦（纵向） -->
      <line
        v-for="i in 6"
        :key="'s' + i"
        :x1="stringX(i - 1)"
        :y1="fretTopY"
        :x2="stringX(i - 1)"
        :y2="fretTopY + 4 * fretH"
        class="diagram-string"
      />

      <!-- 品线（横向）：首品为 nut（加粗），其余正常 -->
      <line
        v-for="i in 5"
        :key="'f' + i"
        :x1="stringX(0)"
        :y1="fretTopY + (i - 1) * fretH"
        :x2="stringX(5)"
        :y2="fretTopY + (i - 1) * fretH"
        :class="['diagram-fret', i === 1 && (voicing.baseFret ?? 1) === 1 ? 'diagram-nut' : '']"
      />

      <!-- X / O 弦标记（弦顶） -->
      <template v-for="(f, i) in voicing.frets" :key="'m' + i">
        <text
          v-if="f === -1"
          :x="stringX(i)"
          :y="fretTopY - 2"
          text-anchor="middle"
          class="diagram-mute"
        >×</text>
        <circle
          v-else-if="f === 0"
          :cx="stringX(i)"
          :cy="fretTopY - 4"
          r="1.6"
          class="diagram-open"
        />
      </template>

      <!-- 横按 -->
      <rect
        v-if="voicing.barre"
        :x="stringX(voicing.barre.from) - 2.2"
        :y="fretCenterY(voicing.barre.fret) - 2.2"
        :width="stringX(voicing.barre.to) - stringX(voicing.barre.from) + 4.4"
        :height="4.4"
        rx="2.2"
        class="diagram-dot diagram-barre"
      />

      <!-- 按弦圆点 -->
      <template v-for="(f, i) in voicing.frets" :key="'d' + i">
        <circle
          v-if="f > 0"
          :cx="stringX(i)"
          :cy="fretCenterY(f - (voicing.baseFret ?? 1) + 1)"
          r="2.4"
          class="diagram-dot"
        />
      </template>
    </template>

    <!-- 未命中指法库：兜底大号文字 -->
    <text
      v-else
      :x="W / 2"
      :y="H / 2 + 6"
      text-anchor="middle"
      class="diagram-fallback"
    >{{ displayName }}</text>
  </svg>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { getVoicing, degreeToChord } from '@/utils/chord'

const props = defineProps<{
  /** 原始和弦文本，可能是级数（"6m"）也可能是和弦名（"Em"）。 */
  chord: string
  /** 调号（弹奏调），用来把级数转成具体和弦。缺省时按 "C" 处理。 */
  capoKey?: string
}>()

// SVG 内部坐标（viewBox），所有几何计算都基于这套坐标。
const W = 40
const H = 52
const fretTopY = 18    // 第一根品线的 Y 坐标
const fretH = 8        // 每品高度
const stringSpacing = 6
const stringLeft = 5

// 实际渲染尺寸（像素）。比 viewBox 小，让浏览器把内容整体缩到 ~75%。
// 调整这两个值就能改变和弦图最终视觉大小，无需动内部布局。
const RENDER_W = 30
const RENDER_H = 40

const stringX = (i: number) => stringLeft + i * stringSpacing
const fretCenterY = (fret: number) => fretTopY + (fret - 0.5) * fretH

/** 把 chord（可能是级数）转换成具体和弦名，用于查表和显示。 */
const resolvedChord = computed(() => degreeToChord(props.chord, props.capoKey || 'C'))
const displayName = computed(() => resolvedChord.value)
const voicing = computed(() => getVoicing(resolvedChord.value))
</script>

<style scoped>
.chord-diagram-svg {
  display: block;
  overflow: visible;
}

.diagram-name {
  fill: var(--accent, #ef4444);
  font-family: Arial, 'Helvetica Neue', sans-serif;
  font-size: 9px;
  font-weight: 700;
  letter-spacing: 0;
}

.diagram-fallback {
  fill: var(--accent, #ef4444);
  font-family: Arial, 'Helvetica Neue', sans-serif;
  font-size: 12px;
  font-weight: 700;
}

.diagram-fretlabel {
  fill: var(--color-font-tertiary, #a1a1aa);
  font-family: Arial, sans-serif;
  font-size: 6.5px;
  font-weight: 500;
}

.diagram-string {
  stroke: var(--color-font-tertiary, #a1a1aa);
  stroke-width: 0.5;
}

.diagram-fret {
  stroke: var(--color-font-tertiary, #a1a1aa);
  stroke-width: 0.5;
}

/* 琴枕（首品线）加粗 */
.diagram-nut {
  stroke: var(--color-font-main, #fafafa);
  stroke-width: 1.4;
}

.diagram-mute {
  fill: var(--color-font-tertiary, #a1a1aa);
  font-family: Arial, sans-serif;
  font-size: 7px;
  font-weight: 700;
}

.diagram-open {
  fill: none;
  stroke: var(--color-font-tertiary, #a1a1aa);
  stroke-width: 0.6;
}

.diagram-dot {
  fill: var(--accent, #ef4444);
}

.diagram-barre {
  fill: var(--accent, #ef4444);
  opacity: 0.92;
}
</style>
