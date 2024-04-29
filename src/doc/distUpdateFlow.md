# Sequence diagrams for updating Piece distances

The main idea in this implementation is to avoid realculating al pieces movements and distances every time e.g. a move occurs, but to find and update only the parts that need to be changed. 

## 1. Spawning a new piece
May be this look like hell :-) but the basic points are rather simple:
- A Square that gets a new Piece tells the new Piece - actually no the pieces, but its "virtual representatives" (vPces) on this square - that it has a distance of 0 and initiates the propagation to neighbours (so finally all distances will be calculated)
- It also tells all other vPces here that something changed here, so they can initiate the recalculation of their distances.
- The distance updates need two steps:
  1. immediately recursively reset (clear) the distances that are invalid (along the USW = unique shortest way)  
  2. initiate the propagation of the new values to neighbours

```mermaid 
sequenceDiagram
    participant ChessBoard
    participant Square
    participant ChessPiece
    box VirtualPieceOnSquare
        participant vPce
        participant neighbourVPce
    end

    Note right of ChessBoard: initiated by initBoardFromFEN() 
    ChessBoard->>+Square: spawnPiece()
    Square->>+vPce: myOwnPieceHasSpawnedHere()
    vPce->>vPce: reset() + dist=0
    vPce->>vPce: setAndPropagateDistance()
    vPce->>+ChessPiece: que(self.doNowPropagate)
    Note right of ChessPiece: ques propagation call
    ChessPiece-->>-vPce: 
    vPce-->>-Square: 

    loop for all other vPces on this square
        Square->>+vPce: pieceHasArrivedHere()
    
        loop recursion to reset distances <br/>over all neighbours until it reaches <br/>such with shorter different paths  
            vPce->>+neighbourVPce: reset()
            neighbourVPce->>neighbourVPce: reset()
            neighbourVPce-->>-vPce: 
        end
        vPce->>+ChessPiece: que(self.doNowPropagate)
        Note right of ChessPiece: ques propagation call
        ChessPiece-->>-vPce: 
    
        vPce-->>-Square: 
    end
    Square->>-ChessBoard: 
    ChessBoard-->ChessBoard: completeCalc() (as in section 2.)
    
```

Note that the actual calculation / propagation of new distance values has not been carried out yet. This is triggered later (e.g. after all pieces are spawned.

## 2. Performing breadth first distance calculation

As shown above, calculation and propagation of distances was not done recursively in the diagram above, as this would lead to a depth first implementation (including running in circles and later knowing there were shorter paths). 

The changes of spawning or moving a piece only leads to the initiating of update processes, by queueing calls to the neighbours. The Piece stored these calls and lets those be executed later on demand of the ChessBoard. This leads to a breadth search.
 
```mermaid
sequenceDiagram
    participant ChessBoard
    participant Square
    participant ChessPiece
    box VirtualPieceOnSquare
        participant vPce
        participant neighbourVPce
    end

    Note right of ChessBoard: initiated by completeCalc() /  continueDistanceCalc()
    loop for all Pieces
        ChessBoard->>+ChessPiece: stepwise per distance: continueDistanceCalc()
        Note right of ChessPiece: takes propagation calls from que
        ChessPiece->>+vPce: doNowPropagate()
        loop recursion to propagate distances over all neighbours
            vPce->>+neighbourVPce: setAndPropagateDistance()
            neighbourVPce->>neighbourVPce: updateRawMinDistances()
            neighbourVPce->>ChessPiece: que(self.doNowPropagate)
            Note right of ChessPiece: ques propagation call
            ChessPiece-->>neighbourVPce: 
            neighbourVPce-->>-vPce: 
        end
        vPce-->>-ChessPiece: 
        ChessPiece-->>-ChessBoard: 
    end
    

```

# 3. Distance updates when a Piece moves

This is similar to spawning, but there is no new piece that needs a new/fresh calculation.

```mermaid 
sequenceDiagram
    participant ChessBoard
    participant Square
    participant ChessPiece
    box VirtualPieceOnSquare
        participant vPce
        participant neighbourVPce
    end

    Note right of ChessBoard: initiated by doMove() -> basicMoveFromTo() 
    ChessBoard->>+Square: update square occupancies (from+to)
    Square->>-ChessBoard: spawnPiece()

    ChessBoard->>+ChessPiece: mover.updateDueToPceMove()
    ChessPiece->>+vPce: myOwnPieceHasMovedHereFrom
    vPce->>vPce: reset() + dist=0
    opt only for sliding pieces
        vPce->>+neighbourVPce: reset() neighbour along the move
        neighbourVPce->>neighbourVPce: reset() until from-square is reached
        neighbourVPce-->>-vPce: 
    end
    vPce->>+neighbourVPce: neighbour-at-frompos.reset+resetPropagation() 
    Note right of neighbourVPce: (reset recursion, like at 1.)
    neighbourVPce-->>-vPce: 

    vPce->>+ChessPiece: que(self.doNowPropagate)
    Note right of ChessPiece: ques propagation call
    ChessPiece-->>-vPce: 
    vPce-->>-ChessPiece: 

    ChessPiece-->>-ChessBoard: 

    loop for all other Pieces
        ChessBoard->>+ChessPiece: mover.updateDueToPceMove()

        ChessPiece->>+vPce: vPceAtFrompos.recalcDistanceAndPropagate()
            vPce->>+ChessPiece: que(self.doNowPropagate)
            Note right of ChessPiece: ques propagation call
            ChessPiece-->>-vPce: 
        vPce-->>-ChessPiece: 

        ChessPiece->>+ChessBoard: intermediate continueDistanceCalc()
        ChessBoard-->>-ChessPiece: 

        ChessPiece->>+neighbourVPce: vPceAtTopos.recalcDistanceAndPropagate()
            neighbourVPce->>+ChessPiece: que(self.doNowPropagate)
            Note right of ChessPiece: ques propagation call
            ChessPiece-->>-neighbourVPce: 
        neighbourVPce-->>-ChessPiece: 
    
        ChessPiece-->>-ChessBoard: 
    end
   
    ChessBoard-->ChessBoard: completeCalc() (as in section 2.)


```

This is followed by the breadth calculation and propagation as described above.