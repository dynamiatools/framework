import { Code, Columns2, Heading, Image, List, MousePointerClick, Minus, MoveVertical, PanelBottom, PanelTop, Quote, Repeat, RectangleVertical, Split, Table, Type } from 'lucide-vue-next';
import type { Component } from 'vue';
import type { BlockType } from '../model/types.js';

/** The icon of every kind of block, shared by the palette, the canvas and the properties panel. */
export const BLOCK_ICONS: Record<BlockType, Component> = {
  header: PanelTop,
  heading: Heading,
  text: Type,
  quote: Quote,
  list: List,
  table: Table,
  image: Image,
  button: MousePointerClick,
  divider: Minus,
  spacer: MoveVertical,
  footer: PanelBottom,
  html: Code,
  columns: Columns2,
  column: RectangleVertical,
  repeat: Repeat,
  condition: Split,
};
