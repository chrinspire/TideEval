# TideEval
**Weird New Chess Algorithm**
- no recursive min-max/alpha-beta tree search through future moves+chessboards at all, 
- move decisions are made based on date explored on the current board 
- evaluating back and forth like the Tide, so to speak

This is only the very beginning of an interesting project - the code might not yet be useful for anyone else and might never be :-P

The class **ChessBasics** might be useful for experimental chess developments in Java. 
You can use it independently of hte rest of the code.

**UI**
Thanks to Timon Ensel it comes with a UI to try it out and play against the algorithm 
  -> type "move" or "automove" in command field.
It also serves to explore the data used to derive the moves.
  -> select chess pieces or squares and / or click on data fields on the right for colorful highlights
