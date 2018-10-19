;
;
;

; standard memory layout: VIC bank 0 (0000-4000), screen at 0400, code vanaf 0800, music op 1000
; beter wellicht VIC bank 1 (4000-8000)
;

SCREEN = $0400

* = $0801

!basic 2018," BY VICIOUS", start

start:
		lda #0
		;;;;sta $d011	; scherm uit
		sta $d020
		sta $d021

		ldx #0
.clear:		lda #32
		sta SCREEN,x
		sta SCREEN+$0100,x
		sta SCREEN+$0200,x
		sta SCREEN+$0300,x
		lda #3+8 ; mc
		sta $D800,x
		sta $D900,x
		sta $DA00,x
		sta $DB00,x
		dex
		bne .clear

		lda #11		; MC kleur 1
		sta $d022
		lda #1		; MC kleur 2
		sta $d023
		lda #%00011000	; screen $0400 (per $400), charset $2000 (per $800) - op $1000 staat ALTIJD charrom
		sta $d018	; screen + charset
		lda #$18
		sta $d016	; multicolor / smoothscroll X
		;;;lda #$9B
		;;;sta $d011	; scherm weer aan / smoothscroll Y
		lda #14
		sta $d020	; DEBUG

		; lets poke a tile
		lda leveltiles+16+0
		sta SCREEN+0
		lda leveltiles+16+1
		sta SCREEN+1
		lda leveltiles+16+2
		sta SCREEN+2
		lda leveltiles+16+3
		sta SCREEN+3

		lda leveltiles+16+4
		sta SCREEN+40
		lda leveltiles+16+5
		sta SCREEN+41
		lda leveltiles+16+6
		sta SCREEN+42
		lda leveltiles+16+7
		sta SCREEN+43
		
		lda leveltiles+16+8
		sta SCREEN+80
		lda leveltiles+16+9
		sta SCREEN+81
		lda leveltiles+16+10
		sta SCREEN+82
		lda leveltiles+16+11
		sta SCREEN+83

		lda leveltiles+16+12
		sta SCREEN+120
		lda leveltiles+16+13
		sta SCREEN+121
		lda leveltiles+16+14
		sta SCREEN+122
		lda leveltiles+16+15
		sta SCREEN+123
		
		; en nu voor 10x5 tiles! --> bouwen waarschijnlijk per row of per column (handig voor scroll)
		
		
mainloop:
		jmp mainloop

;----------------------------------------------------------------
; level data
;----------------------------------------------------------------

leveltiles:	!binary "level-tiles.bin"		; 4x4
levelmap:	!binary "level-map.bin"			; 20x5

; charset

		* = $2000
levelchars:	!binary "level-chars.bin"		; 256*8 = $800

