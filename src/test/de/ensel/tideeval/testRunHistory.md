# History of test results

    2023-06-01:
        lichess_db_puzzle_230601_410-499.csv:  3537 failed,  2830 passed - 54 sec
        lichess_db_puzzle_230601_5xx.csv: 18946 failed, 14815 passed - 4 min 37 sec
        lichess_db_puzzle_230601_2k-410-499.csv: 1065 failed, 935 passed - 20 sec
        lichess_db_puzzle_230601_2k-5xx.csv:     1117 failed, 883 passed - 21 sec
        lichess_db_puzzle_230601_2k-9xx.csv:     1443 failed, 557 passed - 24 sec
        lichess_db_puzzle_230601_2k-12xx.csv:    1541 failed, 459 passed - 24 sec
        lichess_db_puzzle_230601_2k-16xx.csv:    1603 failed, 397 passed - 24 sec
        lichess_db_puzzle_230601_2k-20xx.csv:    1615 failed, 385 passed - 24 sec
     after enabling calcBestMove() to obey checks, king-pins etc.:
        lichess_db_puzzle_230601_2k-410-499.csv: 922 failed, 1078 passed - 16 sec
        lichess_db_puzzle_230601_2k-5xx.csv:     977 failed, 1023 passed - 15 sec
        lichess_db_puzzle_230601_2k-9xx.csv:     1363 failed, 637 passed - 16 sec
        lichess_db_puzzle_230601_2k-12xx.csv:    1437 failed, 563 passed - 19 sec
        lichess_db_puzzle_230601_2k-16xx.csv:    1537 failed, 463 passed - 24 sec
        lichess_db_puzzle_230601_2k-20xx.csv:    1540 failed, 460 passed - 19 sec
     2023-06-03: -> commit+push
        lichess_db_puzzle_230601_2k-410-499.csv: 935 failed, 1065 passed - 17 sec
        lichess_db_puzzle_230601_2k-5xx.csv:    1022 failed,  978 passed - 17 sec
        lichess_db_puzzle_230601_2k-9xx.csv:    1497 failed,  603 passed - 18 sec
        lichess_db_puzzle_230601_2k-12xx.csv:   1494 failed,  506 passed - 19 sec
        lichess_db_puzzle_230601_2k-16xx.csv:   1583 failed,  417 passed - 24 sec
        lichess_db_puzzle_230601_2k-20xx.csv:   1595 failed,  405 passed - 20 sec
     2023-06-05: -first games on lichess !!
        lichess_db_puzzle_230601_2k-410-499.csv: 917 failed, 1083 passed - 20 sec
        lichess_db_puzzle_230601_2k-5xx.csv:    xx failed,  xx passed - 18 sec
        lichess_db_puzzle_230601_2k-9xx.csv:    xx failed,  xx passed - 20 sec
        lichess_db_puzzle_230601_2k-12xx.csv:   xx failed,  xx passed - 20 sec
        lichess_db_puzzle_230601_2k-16xx.csv:   xx failed,  xx passed - 27 sec
        lichess_db_puzzle_230601_2k-20xx.csv:   xx failed,  xx passed - 29 sec
     2023-06-06:
        lichess_db_puzzle_230601_2k-410-499.csv: 876 failed, 1124 passed - 22 sec
        lichess_db_puzzle_230601_2k-9xx.csv:    1309 failed,  691 passed - 29 sec
     2024-06-08:
        lichess_db_puzzle_230601_2k-410-499.csv: 899 failed, 1101 passed - 24 sec
    new:    lichess_db_puzzle_230601_410-499-mateIn1.csv:    1582 failed, 2150 passed - 45 sec
    new:    lichess_db_puzzle_230601_410-499-NOTmateIn1.csv: 1428 failed, 1207 passed - 31 sec
        lichess_db_puzzle_230601_2k-9xx.csv:    1360 failed,  640 passed - 29 sec
        lichess_db_puzzle_230601_2k-20xx.csv:   1538 failed,  462 passed - 29 sec
    2023-06-10:
        lichess_db_puzzle_230601_410-499-mateIn1.csv:    1609 failed, 2123 passed - 46 sec
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv: 1396 failed, 1239 passed - 29 sec
        lichess_db_puzzle_230601_2k-5xx.csv:              982 failed, 1018 passed - 25 sec
        lichess_db_puzzle_230601_2k-9xx.csv:             1342 failed,  658 passed - 25 sec
        lichess_db_puzzle_230601_2k-12xx.csv:            1416 failed,  584 passed - 29 sec
        lichess_db_puzzle_230601_2k-16xx.csv:            1506 failed,  494 passed - 31 sec
        lichess_db_puzzle_230601_2k-20xx.csv:            1520 failed,  480 passed - 30 sec
    Ok-Ok-Ok: Forget the history above, I now noticed only now that the puzzle cvs files first contain a move that
    needs to be done and THEN the puzzle starts... :-o
    --> completely new results in time and quality...:
        lichess_db_puzzle_230601_410-499-mateIn1.csv:    1784 failed, 1948 passed - 70 sec
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv: 1736 failed,  899 passed - 77 sec
        lichess_db_puzzle_230601_2k-5xx.csv:             1212 failed,  788 passed - xx sec
        lichess_db_puzzle_230601_2k-9xx.csv:             1427 failed,  573 passed - 42 sec
        lichess_db_puzzle_230601_2k-20xx.csv:            1581 failed,  419 passed - 45 sec
    + I added a first mateInOne check (which does not yet seem to be perfect, but already does a good job :-)
        lichess_db_puzzle_230601_410-499-mateIn1.csv:    917 failed, 2815 passed - 70 sec
                                        AvoidMateIn1:   2037 failed, 1695 passed - 49 sec   // 1644 are passed even without mate-detection in Square.calcCheckBlockingOptions()
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv: 800 failed, 1835 passed - 44 sec
        lichess_db_puzzle_230601_2k-5xx.csv:             606 failed, 1394 passed - 39 sec
        lichess_db_puzzle_230601_2k-9xx.csv:            1204 failed,  796 passed - 45 sec
        lichess_db_puzzle_230601_2k-12xx.csv:           1397 failed,  603 passed - 49 sec
        lichess_db_puzzle_230601_2k-16xx.csv:           1512 failed,  488 passed - 50 sec
        lichess_db_puzzle_230601_2k-20xx.csv:           1553 failed,  447 passed - 45 sec
    2023-06-16: some more corrections
        lichess_db_puzzle_230601_410-499-mateIn1.csv:    875 failed, 2857 passed - 111 sec
                                        AvoidMateIn1:   2045 failed, 1687 passed - 65 sec   // 1644 are passed even without mate-detection in Square.calcCheckBlockingOptions()
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv: 753 failed, 1882 passed - xx sec
        lichess_db_puzzle_230601_2k-5xx.csv:             590 failed, 1410 passed - xx sec
        lichess_db_puzzle_230601_2k-9xx.csv:            1100 failed,  900 passed - 65 sec
        lichess_db_puzzle_230601_2k-12xx.csv:           1331 failed,  669 passed - 49 sec
        lichess_db_puzzle_230601_2k-16xx.csv:           1456 failed,  544 passed - 68 sec
        lichess_db_puzzle_230601_2k-20xx.csv:           1540 failed,  460 passed - 58 sec
        all: passed 10593 of 20099
    2023-06-17am: some more corrections
        lichess_db_puzzle_230601_410-499-mateIn1.csv:    891 failed, 2841 passed - xx sec
                                        AvoidMateIn1:   1992 failed, 1740 passed - 49 sec
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv: 756 failed, 1879 passed - 44 sec
        lichess_db_puzzle_230601_2k-5xx.csv:             574 failed, 1426 passed - 39 sec
        lichess_db_puzzle_230601_2k-20xx.csv:           1492 failed,  508 passed - 50 sec
    2023-06-18am: pawn beating corrections
        lichess_db_puzzle_230601_410-499-mateIn1.csv:    868 failed, 2864 passed - xx sec
                                        AvoidMateIn1:   2042 failed, 1690 passed - xx sec
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv: 742 failed, 1893 passed - 56 sec
        lichess_db_puzzle_230601_2k-5xx.csv:             562 failed, 1438 passed - 46 sec
        lichess_db_puzzle_230601_2k-9xx.csv:            1009 failed,  991 passed - xx sec
        lichess_db_puzzle_230601_2k-12xx.csv:           1268 failed,  732 passed - 54 sec
        lichess_db_puzzle_230601_2k-16xx.csv:           1417 failed,  583 passed - 56 sec
        lichess_db_puzzle_230601_2k-20xx.csv:           1502 failed,  498 passed - 58 sec
    2023-06-21am:
        lichess_db_puzzle_230601_410-499-mateIn1.csv:    751 failed, 2981 passed - 80 sec
                                        AvoidMateIn1:   2041 failed, 1691 passed - 50 sec
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv: 710 failed, 1925 passed - 51 sec
        lichess_db_puzzle_230601_2k-5xx.csv:             508 failed, 1492 passed - 49 sec
        lichess_db_puzzle_230601_2k-9xx.csv:            1100 failed,  900 passed - 65 sec
        lichess_db_puzzle_230601_2k-12xx.csv:           1233 failed,  767 passed - 50 sec
        lichess_db_puzzle_230601_2k-16xx.csv:           1397 failed,  603 passed - 68 sec
        lichess_db_puzzle_230601_2k-20xx.csv:           1481 failed,  519 passed - 54 sec

    2023-06-22pm:
        lichess_db_puzzle_230601_410-499-mateIn1.csv:    690 failed, 3042 passed - xx sec
                                        AvoidMateIn1:   2156 failed, 1576 passed - xx sec
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv: 669 failed, 1966 passed - 59 sec
        lichess_db_puzzle_230601_2k-5xx.csv:             481 failed, 1519 passed - 50 sec
        lichess_db_puzzle_230601_2k-9xx.csv:            1014 failed,  986 passed - 56 sec
    2023-06-23am: after improvements in calcCheckBlockingOptions()
        lichess_db_puzzle_230601_410-499-mateIn1.csv:    305 failed, 3427 passed - 93 sec
                                        AvoidMateIn1:   2066 failed, 1666 passed - 55 sec
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv: 438 failed, 2197 passed - 55 sec
        lichess_db_puzzle_230601_2k-5xx.csv:             312 failed, 1688 passed - 50 sec
        lichess_db_puzzle_230601_2k-9xx.csv:             942 failed, 1058 passed - 54 sec
    2023-06-23pm: after improvements in calcCheckBlockingOptions()
        lichess_db_puzzle_230601_410-499-mateIn1.csv:    328 failed, 3404 passed - 93 sec
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv: 446 failed, 2635 passed - 55 sec
    2024-06-23Am: after improvements for/against pinning()
        lichess_db_puzzle_230601_410-499-mateIn1.csv:    339 failed, 3393 passed - 82 sec
    2023-06-26am; 3/7 best moves + many more...
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv: 471 failed, 2160 passed - 55 sec
                                        AvoidMateIn1:   1820 failed, 1912 passed - 55 sec
        lichess_db_puzzle_230601_2k-9xx.csv:            1012 failed,  988 passed - 52 sec
    2023-06-26pm: several calc corrections
        lichess_db_puzzle_230601_410-499-mateIn1.csv:    377 failed, 3355 passed - 101 sec
                                        AvoidMateIn1:   1976 failed, 1756 passed - xx sec
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv: 410 failed, 2225 passed - xx sec
        lichess_db_puzzle_230601_2k-5xx.csv:             314 failed, 1686 passed - 48 sec
        lichess_db_puzzle_230601_2k-9xx.csv:             833 failed, 1167 passed - 64 sec
        lichess_db_puzzle_230601_2k-12xx.csv:           1153 failed,  847 passed - 54 sec
        lichess_db_puzzle_230601_2k-16xx.csv:           1338 failed,  662 passed - 63 sec
        lichess_db_puzzle_230601_2k-20xx.csv:           1480 failed,  520 passed - 67 sec
     2023-06-29am: try to cover opponents best move targets
        lichess_db_puzzle_230601_410-499-mateIn1.csv:    356 failed, 3376 passed - 110 sec
                                        AvoidMateIn1:   1833 failed, 1899 passed - 59 sec
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv: 420 failed, 2215 passed - xx sec
        lichess_db_puzzle_230601_2k-9xx.csv:             846 failed, 1154 passed - xx sec
        lichess_db_puzzle_230601_2k-20xx.csv:           1488 failed,  512 passed - 65 sec
    AllTests: 7765 failed, 12490 passed of 20255 tests
     2023-06-29pm: anti-draw evaluations (via board hashes) added
        lichess_db_puzzle_230601_410-499-mateIn1.csv:    362 failed, 3370 passed - 97 sec
                                        AvoidMateIn1:   1848 failed, 1884 passed - 59 sec
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv: 417 failed, 2218 passed - 62 sec
        lichess_db_puzzle_230601_2k-9xx.csv:             851 failed, 1149 passed - xx sec
        lichess_db_puzzle_230601_2k-20xx.csv:           1502 failed,  498 passed - 65 sec
     2023-07-04am - v0.24
        lichess_db_puzzle_230601_410-499-mateIn1.csv:    279 failed, 3453 passed - 98 sec
                                        AvoidMateIn1:   1821 failed, 1911 passed - xx sec
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv: 320 failed, 2315 passed - 66 sec
        lichess_db_puzzle_230601_2k-9xx.csv:             658 failed, 1342 passed - 60 sec
        lichess_db_puzzle_230601_2k-12xx.csv:            965 failed, 1035 passed - 63 sec
        lichess_db_puzzle_230601_2k-20xx.csv:           1403 failed,  597 passed - 66 sec
     2023-07-05am - was never prod
        lichess_db_puzzle_230601_410-499-mateIn1.csv:    223 failed, 3509 passed - 94 sec
                                        AvoidMateIn1:   1758 failed, 1974 passed - xx sec
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv: 278 failed, 2357 passed - 55 sec
        lichess_db_puzzle_230601_2k-9xx.csv:             570 failed, 1430 passed - 57 sec
     2023-07-07pm - hmm, bugs fixed, but really improved?
        lichess_db_puzzle_230601_410-499-mateIn1.csv:    291 failed, 3509 passed - 94 sec
                                        AvoidMateIn1:   1777 failed, 1955 passed - xx sec
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv: 299 failed, 2336 passed - 55 sec
        lichess_db_puzzle_230601_2k-9xx.csv:             565 failed, 1435 passed - 57 sec
     2023-07-07pm - (online as, but not yet pushed) v.25 - added next best move benefit to checking moves
        lichess_db_puzzle_230601_410-499-mateIn1.csv:    279 failed, 3453 passed - 94 sec
                                        AvoidMateIn1:   1776 failed, 1956 passed - xx sec
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv: 294 failed, 2341 passed - 55 sec
        lichess_db_puzzle_230601_2k-9xx.csv:             561 failed, 1439 passed - 63 sec
    slightly bevor last bugfix:
        lichess_db_puzzle_230601_2k-5xx.csv:             266 failed, 1734 passed - 60 sec
        lichess_db_puzzle_230601_2k-12xx.csv:            873 failed, 1127 passed - 70 sec
        lichess_db_puzzle_230601_2k-16xx.csv:           1128 failed,  872 passed - 68 sec
        lichess_db_puzzle_230601_2k-20xx.csv:           1465 failed,  635 passed - 66 sec
     2023-07-09 - pushed v.25 - corrected fork(ish) calculation
        lichess_db_puzzle_230601_410-499-mateIn1.csv:    279 failed, 3453 passed - xx sec
                                        AvoidMateIn1:   1788 failed, 1944 passed - xx sec
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv: 284 failed, 2351 passed - xx sec
        lichess_db_puzzle_230601_2k-9xx.csv:             561 failed, 1439 passed - xx sec
        lichess_db_puzzle_230601_2k-20xx.csv:           1361 failed,  639 passed - xx sec
     2023-07-11 - v.26pre - handles multiple (equal) move origins
        lichess_db_puzzle_230601_410-499-mateIn1.csv:    327 failed, 3405 passed - xx sec
                                        AvoidMateIn1:   1880 failed, 1852 passed - xx sec
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv: 293 failed, 2342 passed - xx sec
        lichess_db_puzzle_230601_2k-9xx.csv:             xx failed, 1439 passed - xx sec
        lichess_db_puzzle_230601_2k-20xx.csv:           xx failed,  639 passed - xx sec
     2023-07-11 - v.26 - some fixes - in sum a little worse! + much slower?
        lichess_db_puzzle_230601_410-499-mateIn1.csv:    336 failed, 3396 passed - 260 sec !?!
                                        AvoidMateIn1:   1897 failed, 1835 passed - xx sec
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv: 341 failed, 2294 passed - xx sec
        lichess_db_puzzle_230601_2k-9xx.csv:             658 failed, 1342 passed - 156 sec // mit MAX 4P 11B
        lichess_db_puzzle_230601_2k-9xx.csv:             636 failed, 1364 passed - 166 sec // mit MAX 7P 20B
        lichess_db_puzzle_230601_2k-9xx.csv:             636 failed, 1364 passed - 130 sec // mit MAX 7P 20B - RemeberPredecessor activated
        lichess_db_puzzle_230601_2k-9xx.csv:             636 failed, 1364 passed - 120 sec // mit MAX 7P 20B - RemeberFirstMovesToHere activated
        lichess_db_puzzle_230601_2k-20xx.csv:           1352 failed,  648 passed - 184 sec
     2023-07-13am - v.27 - introduce new mobility + slighly take it into account in move benefits  // all with MAX 4P 11B
        lichess_db_puzzle_230601_410-499-mateIn1.csv:    319 failed, 3414 passed - xx sec
        lichess_db_puzzle_230601_2k-9xx.csv:             618 failed, 1382 passed - 109 sec
        lichess_db_puzzle_230601_2k-20xx.csv:           1342 failed,  658 passed - 104 sec
     2023-07-14pm - v.28pre
        lichess_db_puzzle_230601_410-499-mateIn1.csv:    321 failed, 3412 passed - xx sec
                                        AvoidMateIn1:   1863 failed, 1869 passed - 99 sec
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv: 314 failed, 2321 passed - 93 sec
        lichess_db_puzzle_230601_2k-5xx.csv:             274 failed, 1726 passed - 81 sec
        lichess_db_puzzle_230601_2k-9xx.csv:             582 failed, 1418 passed - 95 sec
        lichess_db_puzzle_230601_2k-12xx.csv:            897 failed, 1103 passed - 105 sec
        lichess_db_puzzle_230601_2k-16xx.csv:           1194 failed,  806 passed - 107 sec
        lichess_db_puzzle_230601_2k-20xx.csv:           1364 failed,  636 passed - 106 sec
     2023-07-15am - v.28
        lichess_db_puzzle_230601_410-499-mateIn1.csv:    280 failed, 3452 passed - 150 sec
                                        AvoidMateIn1:   1871 failed, 1861 passed - 94 sec
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv: 462 failed, 2171 passed - 93 sec
        lichess_db_puzzle_230601_2k-5xx.csv:             337 failed, 1662 passed - 76 sec
        lichess_db_puzzle_230601_2k-9xx.csv:             650 failed, 1350 passed - 86 sec
        lichess_db_puzzle_230601_2k-12xx.csv:            897 failed, xx passed - 105 sec
        lichess_db_puzzle_230601_2k-16xx.csv:           1194 failed,  xx passed - 107 sec
        lichess_db_puzzle_230601_2k-20xx.csv:           1364 failed,  xx passed - 106 sec
     2023-07-16am - v.28ck2 - several evaluation+futirelevel corrections - but worse?
        lichess_db_puzzle_230601_410-499-mateIn1.csv:    367?? 422? failed, 3365 passed - xx sec
                                        AvoidMateIn1:   1820 failed, 1912 passed - xx sec
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv: 582 failed, 2053 passed - 90 sec
        lichess_db_puzzle_230601_2k-5xx.csv:             410 failed, 1590 passed - 76 sec
        lichess_db_puzzle_230601_2k-9xx.csv:             730 failed, 1270 passed - 88 sec
        lichess_db_puzzle_230601_2k-12xx.csv:            955 failed, 1045 passed - 102 sec
        lichess_db_puzzle_230601_2k-16xx.csv:           1202 failed,  798 passed - xx sec
        lichess_db_puzzle_230601_2k-20xx.csv:           1375 failed,  625 passed - 102 sec
     2023-07-18pm - v.28ck5
        lichess_db_puzzle_230601_410-499-mateIn1.csv:    439 failed, 3293 passed - 148 sec
                                        AvoidMateIn1:   1868 failed, 1864 passed - xx sec
        lichess_db_puzzle_230601_2k-9xx.csv:             741 failed, 1259 passed - 98 sec
     2023-07-21am - v.29pre1              // with coveringVPce.addClashContrib(-benefit);   // and without
        lichess_db_puzzle_230601_410-499-mateIn1.csv:    488 failed, 3732 passed - 154 sec    474 f
                                        AvoidMateIn1:   1843 failed, 1889 passed - 97 sec    1853 f
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv: 590 failed, 2045 passed - 93 sec     587 failed
        lichess_db_puzzle_230601_2k-9xx.csv:             757 failed, 1243 passed - 98 sec     756 f
     same but with setShortest... statt getPredecessors in trapping-code                    // without
        lichess_db_puzzle_230601_410-499-mateIn1.csv:    453 failed, 3732 passed - 154 sec    453 f
                                        AvoidMateIn1:   1867 failed, 1889 passed - 97 sec    1867 f
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv: 566 failed, 2069 passed - 93 sec     566 failed
        lichess_db_puzzle_230601_2k-9xx.csv:             744 failed, 1243 passed - 98 sec     744 f
     2023-07-23am - v.29
        lichess_db_puzzle_230601_410-499-mateIn1.csv:    445 failed, 3287 passed - xx sec
                                        AvoidMateIn1:   1866 failed, 1866 passed - xx sec
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv: 598 failed, 2037 passed - 92 sec
        lichess_db_puzzle_230601_2k-9xx.csv:             728 failed, 1272 passed - xx sec
        lichess_db_puzzle_230601_2k-12xx.csv:            950 failed, 1050 passed - xx sec
        lichess_db_puzzle_230601_2k-20xx.csv:           1328 failed,  672 passed - 89 sec
     2023-08-01 - v.29d (with !emptySquare instead of isEmptySquare in addChance...)
        lichess_db_puzzle_230601_410-499-mateIn1.csv:    396 failed, 3336 passed - 151 sec
                                        AvoidMateIn1:   1893 failed, 1839 passed - 79 sec
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv: 637 failed, 1998 passed - 92 sec
        lichess_db_puzzle_230601_2k-9xx.csv:             727 failed, 1273 passed - 80 sec
        lichess_db_puzzle_230601_2k-12xx.csv:            946 failed, 1054 passed - 89 sec
     2023-08-01 - v.29e
        lichess_db_puzzle_230601_410-499-mateIn1.csv:    406 failed, 3326 passed - 133 sec
                                        AvoidMateIn1:   1913 failed, 1819 passed - 77 sec
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv: 562 failed, 2073 passed - xx sec
        lichess_db_puzzle_230601_2k-9xx.csv:             727 failed, 1273 passed - 79 sec
        lichess_db_puzzle_230601_2k-12xx.csv:            942 failed, 1058 passed - 82 sec
        lichess_db_puzzle_230601_2k-20xx.csv:           1347 failed,  653 passed - 87 sec
     2023-08-01 - v.29h
        Score of 0.26 cs 0.29i: 20 - 24 - 36
        Score of SF14.1/0ply vs. 0.29h: 78 - 0 - 2
        Score of SF14.1/4ply/1600 vs. 0.29h: 324 - 31 - 45
        lichess_db_puzzle_230601_410-499-mateIn1.csv:    391 failed, 3341 passed - 140 sec

     2023-08-02 - v.29i
        Score of 0.26 cs 0.29i: 21 - 25 - 34
        Score of SF14.1/0ply vs. 0.29i: 79 - 1 - 0
        Score of SF14.1/4ply/1600 vs. 0.29i: 328 - 31 - 41
        lichess_db_puzzle_230601_410-499-mateIn1.csv:    399 failed, 3333 passed - 139 sec
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv: 510 failed, 2125 passed - 77 sec
        lichess_db_puzzle_230601_2k-9xx.csv:             713 failed, 1287 passed - 76 sec

     2023-08-08 - v.30pre1 - test with different moveEval-Comparison, considering futureLevels more
        better in non-mateIn1-puzzles,
            BUT Score of 0.29i cs 0.30pre1: 58 - 5 - 17
            AND Score of 0.26 cs 0.30pre1: 54 - 13 - 13
            AND Score of SF14.1/4ply/1600 vs. 0.30pre1: 382 - 9 - 9
        lichess_db_puzzle_230601_410-499-mateIn1.csv:    441 failed, 3291 passed - 138 sec
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv: 454 failed, 2181 passed - 82 sec
        lichess_db_puzzle_230601_2k-9xx.csv:             674 failed, 1326 passed - 84 sec

    2023-08-08 - v.29j
        Score of 0.26 cs 0.29j: 15 - 30 - 35
        Score of SF14.1/0ply vs. 0.29j: 76 - 1 - 3
        Score of SF14.1/4ply/1600 vs. 0.29j: 324 - 23 - 53
        lichess_db_puzzle_230601_410-499-mateIn1.csv:    398 failed, 3334 passed - xx sec
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv: 508 failed, 2127 passed - xx sec
        lichess_db_puzzle_230601_2k-9xx.csv:             724 failed, 1276 passed - xx sec
    2023-08-10 - v.29k
        Score of 0.26 vs. 0.29k: 17 - 30 - 33
        Score of SF14.1/0ply vs. 0.29k: 75 - 1- 4
        Score of SF14.1/4ply/1600 vs. 0.29k: 309 - 41 - 50
        resp. Score of SF14.1/4ply/1600 vs. 0.29k: 323 - 32 - 45
        lichess_db_puzzle_230601_410-499-mateIn1.csv:    409 failed, 3732 passed - xx sec
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv: 522 failed, 2113 passed - 79 sec
        lichess_db_puzzle_230601_2k-9xx.csv:             733 failed, 1267 passed - 78 sec
     2023-08-10 - v.29m
        Score of 0.26 vs. 0.29m: 20 - 29 - 31
        Score of SF14.1/0ply vs. 0.29m: 73 - 2 - 5
        Score of SF14.1/4ply/1600 vs. 0.29m: 313 - 40 - 47
        resp. Score of SF14.1/4ply/1600 vs. 0.29m:
        lichess_db_puzzle_230601_410-499-mateIn1.csv:    399 failed, 3333 passed - xx sec
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv: 522 failed, 2113 passed - 82 sec
        lichess_db_puzzle_230601_2k-9xx.csv:             726 failed, 1267 passed - xx sec
     2023-08-10 - v.29p-pre
        Score of 0.26 vs TideEval 0.29p: 17 - 24 - 39  [0.456] 80
        Score of SF14.1/0ply vs. 0.29m: 73 - 3 - 4
        Score of SF14.1/4ply/1600 vs. 0.29m: 310 - 33 - 57
     2023-08-10 - v.29p
        Score of 0.26 vs TideEval 0.29p: 10 - 26 - 44  [0.400] 80
        Score of SF14.1/0ply vs. 0.29m?: 71 - 2 - 7
        Funfact: Score of Stockfish **11 64** vs TideEval 0.29p: 79 - 0 - 1  [0.994] 80
        Score of SF14.1/4ply/1600 vs. 0.29m?: 304 - 34 - 62
        lichess_db_puzzle_230601_2k-9xx.csv:             716 failed, 1284 passed - 87 sec
    with changed mobility benefits:
        Score of TideEval 0.26 vs TideEval 0.29p: 26 - 22 - 32  [0.525] 80
        Score of SF14.1/0ply vs. 0.29m: 75 - 1 - 4
    + change in pawn-promotion-defence
        Score of TideEval 0.26 vs TideEval 0.29p: 25 - 23 - 32  [0.512] 80
        Score of SF14.1/0ply vs. 0.29p: 74 - 2 - 4
        Score of SF14.1/4ply/1600 vs. 0.29p: 318 - 32 - 50

    2023-08-10 - v.29q (change in pawn-promotion-defence, but undid mobility change)
        Score of 0.26 vs TideEval 0.29q:                13 - 25 - 42
        Score of SF14.1/0ply vs. 0.29q:                 68 - 4 - 8
        Score of SF14.1/4ply/1600 vs. 0.29q:           328 - 32 - 40
                                                       321 - 32 - 47
        Score of *SF11-64/0ply vs TideEval 0.29q:       79 - 0 - 1
        Score of *SF11-64/4ply/1600 vs TideEval 0.29q: 343 - 25 - 32
                                                       348 - 14 - 38
        lichess_db_puzzle_230601_410-499-mateIn1.csv:    403 failed, 3329 passed - 143 sec
                                        AvoidMateIn1:   1914 failed, 1818 passed - 80 sec
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv: 517 failed, 2118 passed - 98 sec
        lichess_db_puzzle_230601_2k-9xx.csv:             717 failed, 1283 passed - xx sec

     2023-08-10 - v.29r with addBenefitBlocker changes (incl. same color and not moving on turning points)
        Score of 0.26 vs TideEval 0.29r:                24 - 26 - 30
        Score of SF14.1/0ply vs. 0.29r:                 75 - 0 - 5
        Score of SF14.1/4ply/1600 vs. 0.29r:           333 - 34 - 33
        Score of *SF11-64/0ply vs TideEval 0.29r:       79 - 0 - 1
        Score of *SF11-64/4ply/1600 vs TideEval 0.29r: 355 - 22 - 23
        lichess_db_puzzle_230601_410-499-mateIn1.csv:    542 failed, - passed - xx sec
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv: 583 failed, 2052 passed - xx sec
        lichess_db_puzzle_230601_2k-9xx.csv:             763 failed, - passed - xx sec

     2023-08-10 - v.29r without the above addBenefitBlocker changes
        lichess_db_puzzle_230601_410-499-mateIn1.csv:    441 failed, - passed - xx sec
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv: 589 failed, 2052 passed - xx sec
        lichess_db_puzzle_230601_2k-9xx.csv:             762 failed, - passed - xx sec

     2023-08-10 - v.29r with parts of the above addBenefitBlocker changes (no same color approach, which does make it worse it seems)
        Score of 0.26 vs TideEval 0.29r:                24 - 31 - 25
        Score of SF14.1/0ply vs. 0.29r:                 77 - 0 - 3
        Score of SF14.1/4ply/1600 vs. 0.29r:
        Score of *SF11-64/0ply vs TideEval 0.29r:       80 - 00 - 0
        Score of *SF11-64/4ply/1600 vs TideEval 0.29r:
        lichess_db_puzzle_230601_410-499-mateIn1.csv:    448 failed, - passed - xx sec
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv: 584 failed, 2052 passed - xx sec
        lichess_db_puzzle_230601_2k-9xx.csv:             757 failed, 1243 passed - 79 sec

     2023-08-10 - v.29s (r + some undos+small corrections) hmm ->>2     -(>>2+>> 3)        ->>3         -0
        Score of 0.26 vs TideEval 0.29s:                20 - 25 - 35   21 - 21 - 38     20 - 26 - 34    21 - 21 - 38
        Score of SF14.1/0ply vs. 0.29s:                 77 -  1 - 2    77 -  1 - 2      76 -  2 - 2     75 -  3 - 2
        Score of SF14.1/4ply/1600 vs. 0.29s:           338 - 29 - 33  323 - 37 - 40    317 - 34 - 49   319 - 28 - 53
        Score of *SF11-64/0ply vs TideEval 0.29s:       80 -  0 - 0    80 -  0 - 0      80 -  0 - 0     80 -  0 - 0
        Score of *SF11-64/4ply/1600 vs TideEval 0.29s: 356 - 16 - 28  344 - 19 - 37    345 - 22 - 33   349 - 17 - 34
        lichess_db_puzzle_230601_410-499-mateIn1.csv:      441            413               411           418 failed
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:   522            517               523           523 failed
        lichess_db_puzzle_230601_2k-9xx.csv:               723            730               723           722 failed

    2023-08-10 - v.29t (unsing ->>4 = almost nothing, see to table above) + skip conditional additionalAttackers
        Score of 0.26 vs TideEval 0.29t:                 21 - 23 - 36  -> same    to compare with ->>2 18 - 25 - 37-> better
        Score of SF14.1/0ply vs. 0.29t:                  77 -  2 - 1   -> worse                        77 -  1 - 2 -> better
        Score of SF14.1/4ply/1600 vs. 0.29t:            310 - 35 - 55  -> better                      324 - 34 - 42-> worse
        Score of *SF11-64/0ply vs TideEval 0.29t:        80 -  0 - 0   -> same                         80 -  0 - 0 -> same
        Score of *SF11-64/4ply/1600 vs TideEval 0.29t:  358 - 11 - 31  -> worse                       341 - 26 - 33-> better
        lichess_db_puzzle_230601_410-499-mateIn1.csv:        415 failed -> same                             419 -> same (4 worse)
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:     522 failed -> same (1 better)              => -(>> 3) will be used
        lichess_db_puzzle_230601_2k-9xx.csv:                 722 failed -> same (1 better)

    2023-08-10 - v.29u - special benefit for in uncovarable additional attacks
        Score of 0.26 vs TideEval 0.29t:                 21 - 25 - 34  -> 1 worse
        Score of SF14.1/0ply vs. 0.29t:                  76 -  2 - 2   -> 1 better
        Score of SF14.1/4ply/1600 vs. 0.29t:            322 - 32 - 46  -> 11 worse
        Score of *SF11-64/0ply vs TideEval 0.29t:        80 -  0 - 0   -> same
        Score of *SF11-64/4ply/1600 vs TideEval 0.29t:   356 - 15 - 29  -> 2 better
        lichess_db_puzzle_230601_410-499-mateIn1.csv:        419 failed -> same
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:     517 failed -> same (5 better)
        lichess_db_puzzle_230601_2k-9xx.csv:                 724 failed -> same (2 worse)

    2023-08-10 - v.29v - reduce checking + hindering benefits as long as unclear if blockable
        Score of 0.26 vs TideEval 0.29v:                 21 - 26 - 33  -> same (0.5 worse)
        Score of SF14.1/0ply vs. 0.29v:                  77 -  1 - 2   -> same (0.5 worse)
        Score of SF14.1/4ply/1600 vs. 0.29v:            309 - 43 - 48  -> better
        Score of *SF11-64/0ply vs TideEval 0.29v:        80 -  0 - 0   -> same
        Score of *SF11-64/4ply/1600 vs TideEval 0.29v:   353 - 22 - 25  -> same (0.5 worse)
        lichess_db_puzzle_230601_410-499-mateIn1.csv:        450 failed -> worse
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:     608 failed -> worse
        lichess_db_puzzle_230601_2k-9xx.csv:                 761 failed -> worse

    2023-08-10 - v.29w - adhere contribution blocking
        Score of 0.26 vs TideEval 0.29w:                 19 - 28 - 33  -> 1 better
        Score of SF14.1/0ply vs. 0.29w:                  75 -  2 - 3   -> 1.5 better
        Score of SF14.1/4ply/1600 vs. 0.29w:            318 - 35 - 47  -> 5 worse
        Score of *SF11-64/0ply vs TideEval 0.29w:        80 -  0 - 0   -> same :-(
        Score of *SF11-64/4ply/1600 vs TideEval 0.29w:   359 - 16 - 25  -> 3 worse
        lichess_db_puzzle_230601_410-499-mateIn1.csv:        449 failed -> same (1 better )
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:     610 failed -> same (2 worse)
        lichess_db_puzzle_230601_2k-9xx.csv:                 767 failed -> 7 worse

    2023-08-10 - v.29x
        Score of *SF11-64/4ply/1600 vs TideEval 0.29x:   354 - 12 - 34  -> 11 better
        lichess_db_puzzle_230601_410-499-mateIn1.csv:        441 failed -> better
        lichess_db_puzzle_230601_2k-9xx.csv:                 735 failed -> better

    2023-08-10 - v.29y
        Score of 0.26 vs TideEval 0.29y:                 19 - 23 - 38  -> 2.5 better
        Score of SF14.1/0ply vs. 0.29y:                  75 -  1 - 4   -> 0.5 better
        Score of SF14.1/4ply/1600 vs. 0.29y:            320 - 35 - 45  -> 2 worse
        Score of *SF11-64/0ply vs TideEval 0.29y:        79 -  0 - 1   -> finally 1 (again)...
        Score of *SF11-64/4ply/1600 vs TideEval 0.29y:  351 - 15 - 34  -> 1.5 better
        lichess_db_puzzle_230601_410-499-mateIn1.csv:        441 failed -> same
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:     x608 failed ->
        lichess_db_puzzle_230601_2k-9xx.csv:                 736 failed -> same (1 worse)
    2023-08-15 - v.29zpre
        Score of 0.26 vs TideEval 0.29zpre:              15 - 26 - 39  -> 2.5 better
        Score of SF14.1/0ply vs. 0.29zpre:               76 -  1 - 3   -> 1 worse
        Score of SF14.1/4ply/1600 vs. 0.29zpre:         323 - 33 - 44  -> 2 worse
                                                        318 - 29 - 53  -> 5 better
                                                        311 - 38 - 51  -> 14 better
                                                   avg. 317 - 34 - 49 -> 3.5 better
        Score of *SF11-64/0ply vs TideEval 0.29zpre:     80 -  0 - 0   -> worse (again)...
        Score of *SF11-64/4ply/1600 vs TideEval 0.29zpre:334- 27 - 39  -> 11 better
                                                         352- 16 - 32  -> 1.5 worse
                                                         350- 22 - 28  -> 2.5 worse
                                                   avg.  345- 22 - 33 -> 2.5 better
        lichess_db_puzzle_230601_410-499-mateIn1.csv:        438 failed -> 3 better
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:     516 failed -> better
        lichess_db_puzzle_230601_2k-9xx.csv:                 736 failed -> same
    2023-08-15 - v.29z1
        Score of 0.26 vs TideEval 0.29z1:                 19 - 29 - 32  -> 5.5 worse
        Score of SF14.1/0ply vs. 0.29z1:                  75 -  1 - 4   -> same
        Score of SF14.1/4ply/1600 vs. 0.29z1:            312 - 37 - 51  -> better
                                                         315 - 29 - 59  -> better
                                                     avg.313.5- 33- 55
        Score of *SF11-64/0ply vs TideEval 0.29z1:        80 -  0 - 0   -> same
        Score of *SF11-64/4ply/1600 vs TideEval 0.29z1:  350 - 13 - 37  -> 0.5 worse
        lichess_db_puzzle_230601_410-499-mateIn1.csv:        451 failed -> 13 worse
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:     551 failed -> 35 worse
        lichess_db_puzzle_230601_2k-9xx.csv:                 737 failed -> same (1 worse)

    2023-08-15 - v.29z2 - +change relEval on kings square -> chenged to z3, but same -> reverted
        Score of 0.26 vs TideEval 0.29z2:                 20 - 30 - 30  -> 2 worse
        Score of SF14.1/0ply vs. 0.29z2:                  75 -  1 - 4   -> same
        Score of SF14.1/4ply/1600 vs. 0.29z2:            331 - 24 - 45  -> 16 worse
                                                         318 - 30 - 52  -> 7 worse
        Score of *SF11-64/0ply vs TideEval 0.29z2:        80 -  0 - 0   -> same
        Score of *SF11-64/4ply/1600 vs TideEval 0.29z2:  347 - 12 - 41  -> 7 better
                                                         354 - 14 - 32  -> 5.5 worse
    2023-08-15 - v.29z4 still incl. z3 code
        Score of 0.26 vs TideEval 0.29z4:                 24 - 26 - 30  -> 2.5 worse compared to z1
        Score of SF14.1/0ply vs. 0.29z4:                  73 -  2 - 5   -> 1.5 better
        Score of SF14.1/4ply/1600 vs. 0.29z4:            338 - 27 - 35  ->  worse
        Score of *SF11-64/0ply vs TideEval 0.29z4:        80 -  x - 0   -> same
        Score of *SF11-64/4ply/1600 vs TideEval 0.29z4:  352 - 13 - 35  -> 2 worse
    => back to 0.29z1

    2023-08-10 - v.29z5 with BAD_addBenefitToBlockers()
        Score of 0.26 vs TideEval 0.29z5:                 24 - 21 - 35  -> 1 worse compared to z1
        Score of SF14.1/0ply vs. 0.29z5:                  77 -  2 - 1   -> 2.5 worse
        Score of SF14.1/4ply/1600 vs. 0.29z5:            330 - 28 - 42  -> 15 worse
        Score of *SF11-64/0ply vs TideEval 0.29z5:        78 -  0 - 2   -> 2 better!
        Score of *SF11-64/4ply/1600 vs TideEval 0.29z5:  360 - 13 - 27  -> 10 worse
        lichess_db_puzzle_230601_410-499-mateIn1.csv:        568 failed -> >100 worse
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:     x608 failed ->
        lichess_db_puzzle_230601_2k-9xx.csv:                 x767 failed -> 30 worse

    2023-08-10 - v.29z5
        Score of 0.26 vs TideEval 0.29z5:                 24 - 19 - 37  -> same compared to z1
        Score of SF14.1/0ply vs. 0.29z5:                  74 -  1 - 5   -> 1 better
        Score of SF14.1/4ply/1600 vs. 0.29z5:            319 - 24 - 57  -> 2 worse
                                                         323 - 27 - 50  -> 5.5 worse
                                                         324 - 24 - 52  -> 5 worse
                                                    avg. 322 - 25 - 53
        Score of *SF11-64/0ply vs TideEval 0.29z5:        78 -  0 - 2   -> 2 better!
        Score of *SF11-64/4ply/1600 vs TideEval 0.29z5:  354 - 11 - 35  -> 3 worse
                                                         344 - 15 - 41  -> 5 better
                                                         350 - 10 - 40  -> 1.5 better
                                                    avg. 349 - 12 - 39
        lichess_db_puzzle_230601_410-499-mateIn1.csv:        671 failed -> >200 worse
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:     539 failed -> 12 better
        lichess_db_puzzle_230601_2k-9xx.csv:                 810 failed -> 73 worse
   -> not really better, but calculation more consistent, so we leave it

    2023-08-10 - v.29z6 - baseline - warning of .29z7 below turned off
        Score of 0.26 vs TideEval 0.29z6:                 24 - 25 - 31  -> 3 worse compared to z1                  z1:   19 - 29 - 32
        Score of SF14.1/0ply vs. 0.29z6:                  73 -  1 - 6   -> 2 better                                      75 -  1 - 4
        Score of SF14.1/4ply/1600 vs. 0.29z6:            318 - 20 - 62  -> 8 better                                     312 - 37 - 51
                                                         319 - 18 - 63  -> 8 better                                     315 - 29 - 59
                                                                                                                   avg. 313.5- 33- 55
        Score of *SF11-64/0ply vs TideEval 0.29z6:        77 -  0 - 3   -> 3 better                                     80 -  0 - 0
        Score of *SF11-64/4ply/1600 vs TideEval 0.29z6:  350 - 10 - 40  -> 1.5 better                              z1:  350 - 13 - 37
                                                         351 - 14 - 35  -> 1.5 worse
        lichess_db_puzzle_230601_410-499-mateIn1.csv:        663 failed -> 211 worse                                        451 failed
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:     532 failed -> 19 better                                        551 failed
        lichess_db_puzzle_230601_2k-9xx.csv:                 770 failed -> 33 worse                                         737 failed

    2023-08-10 - v.29z7 - warn about running piece into check fork   >>2                                >>4
        Score of 0.26 vs TideEval 0.29z7:                 23 - 27 - 30  -> same compared to z6     22 - 27 - 31  -> 1 better compared to z6
        Score of SF14.1/0ply vs. 0.29z7:                  77 -  0 - 3   -> 3.5 worse               71 -  1 - 8   -> 2 better
        Score of SF14.1/4ply/1600 vs. 0.29z7:            336 - 15 - 49  -> 20 worse               321 - 25 - 54  -> 5 worse
        Score of *SF11-64/0ply vs TideEval 0.29z7:        79 -  0 - 1   -> 2 worse                 77 -  0 - 3   -> same
        Score of *SF11-64/4ply/1600 vs TideEval 0.29z7:  340 - 18 - 42  -> 6 better               347 - 14 - 39  -> 3 better
        lichess_db_puzzle_230601_410-499-mateIn1.csv:        665 failed -> same (2 worse)
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:     533 failed -> same (1 worse)
        lichess_db_puzzle_230601_2k-9xx.csv:                 770 failed -> same

    2023-08-10 - v.29z10-13 - (z7 with >>4) +  loosing clash contribs is accounted only 1) 0 vs. 2) little 3) very little any more, if my move takes a piece that also has a contribution in the same clash
        Score of 0.26 vs TideEval 0.29z10:                19 - 26 - 35  -> 3.5 better comp.to z7>>4   20 - 31 - 29 -> -2.5 comp.to z10 21 - 32 - 27  -> -5  comp.to z10 23 - 28 - 29  -> -5 comp.to z10
        Score of SF14.1/0ply vs. 0.29z10:                 76 -  1 - 3   -> 2.5 worse                  77 -  1 - 2  -> -0.5             76 -  1 - 3   -> =               76 -  1 - 3   -> =
        Score of SF14.1/4ply/1600 vs. 0.29z10:           308 - 24 - 68  -> 13.5 better             318.5 -23.5- 58 -> -10            317. - 22 - 60. -> -9             322. - 22 - 55 -> -13
        Score of *SF11-64/0ply vs TideEval 0.29z10:       79 -  0 - 1   -> 2 worse                    79 -  0 - 1  -> same             79 -  0 - 1   -> =               79 -  0 - 1   -> =
        Score of *SF11-64/4ply/1600 vs TideEval 0.29z10: 355 - 11 - 34  -> 6.5 worse                 341 - 12 - 47 -> +13            351. - 12.- 36  -> +3.5           346 - 10 - 44  -> +9.5
        lichess_db_puzzle_230601_410-499-mateIn1.csv:       642 failed -> 23 better                      642  =                             642      -> =                    642      -> =
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:    517 failed -> 16 better                      519  -2                            517      -> =                    517      -> =
        lichess_db_puzzle_230601_2k-9xx.csv:                762 failed -> same (8 better)                766  -4                            772      -> -10                  763      -> -1

    2023-08-10 - v.29z14 - reduce benefit of additional caverage      + z15 reversePieceBenefit + reduce more if already "overcovered" + z16 do not fully loose pawnDoubleHopBenefits with omaxbenefits + z17 queens magic rect triangle
        Score of 0.26 vs TideEval 0.29z14:                 23 - 25 - 32  -> 1.5 better compared to z13  18 - 22 - 40 -> 6.5 better comp. to z14      14 - 26 - 40 -> +2  comp to z15       18 - 22 - 40 -> -2  comp to z16
        Score of SF14.1/0ply vs. 0.29z14:                  74 -  3 - 3   -> 1 better                    76 -  2 - 2  -> 1.5 worse                    74 -  3 - 3  -> +1.5                  74 -  1 - 5  -> +
        Score of SF14.1/4ply/1600 vs. 0.29z14:            321 - 20. - 58. -> 2.5 better                326. - 18 - 55. -> 1 worse                   323. - 24 - 52.-> =                   324 - 23 - 53 -> =
        Score of *SF11-64/0ply vs TideEval 0.29z14:        79 -  0 - 1   -> same                        79 -  0 - 1  -> same                         79 -  0 - 1  ->  =                    78 -  0 - 2  ->  +1 !!
        Score of *SF11-64/4ply/1600 vs TideEval 0.29z14:  347. - 10 - 42.  -> 1.5 worse                347.- 13. - 39-> 1.5 worse                   345.- 12. - 42->  +2.5                351 - 10. - 38. ->  -4.5
        lichess_db_puzzle_230601_410-499-mateIn1.csv:        x failed -> same                                                                            642      -> = comp to z14 above      642       -> = comp to z16
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:     x failed -> same                                                                            522      -> -5                       526       -> -4
        lichess_db_puzzle_230601_2k-9xx.csv:                 x failed -> same                                                                            753      -> +10                      753       -> =

    2023-08-10 - v.29z18 - reduce knight mobility values                                                z17:
        Score of 0.26 vs TideEval 0.29z18:                 17 - 23 - 40  -> 0.5 better comp.to z17    18 - 22 - 40
        Score of SF14.1/0ply vs. 0.29z18:                  76 -  2 - 2   -> 2.5 worse                 74 -  1 - 5
        Score of SF14.1/4ply/1600 vs. 0.29z18:            325 - 11 - 64 -> 5 better                  324 - 23 - 53
        Score of *SF11-64/0ply vs TideEval 0.29z18:        79 -  0 - 1   -> -1                        78 -  0 - 2
        Score of *SF11-64/4ply/1600 vs TideEval 0.29z18:  346.- 11.- 42  -> 4.5 better                351 - 10.- 38.
        lichess_db_puzzle_230601_410-499-mateIn1.csv:        652 failed -> -10
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:     526 failed -> =
        lichess_db_puzzle_230601_2k-9xx.csv:                 760 failed -> -7
        lichess_db_puzzle_230601_2k-12xx.csv:                967 failed

    2023-08-10 - v.29z19 - a little more castling motivation + king castle area clearance
        Score of 0.26 vs TideEval 0.29z19(here still w/o clearance):   21 - 19 - 40  -> -2 comp. to v0.29z18 (strange, everything else alsmost same)
        Score of 0.26 vs TideEval 0.29z19:                 21 - 19 - 40  -> -2 comp. to v0.29z18 still...
        Score of SF14.1/0ply vs. 0.29z19:                  76 -  2 - 2   -> -2.5
        Score of SF14.1/4ply/1600 vs. 0.29z19:            324 - 20.- 55. -> -4
        Score of *SF11-64/0ply vs TideEval 0.29z19:        79 -  0 - 1   -> =
        Score of *SF11-64/4ply/1600 vs TideEval 0.29z19:  341.- 10.- 48  -> +6
        lichess_db_puzzle_230601_410-499-mateIn1.csv:        652 failed  -> =
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:     x526 failed ->
        lichess_db_puzzle_230601_2k-9xx.csv:                 x760 failed ->

    2023-08-10 - v.30 - corrected king trapping benefit, where it is still unclear if really trapped
        Score of 0.26 vs TideEval 0.30:                 19 - 22 - 39  -> +0.5 comp. to v0.29z19
        Score of SF14.1/0ply vs. 0.30:                  76 -  2 - 2   -> =
        Score of SF14.1/4ply/1600 vs. 0.30:            321 - 25 - 54  -> +1
        Score of *SF11-64/0ply vs TideEval 0.30:        79 -  0 - 1   -> =
        Score of *SF11-64/4ply/1600 vs TideEval 0.30:  351.- 14 - 34.  -> -12 (!)
        lichess_db_puzzle_230601_410-499-mateIn1.csv:         397 failed -> +255 (!)
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:      501 failed -> +25
        lichess_db_puzzle_230601_2k-9xx.csv:                  682 failed -> +78
        lichess_db_puzzle_230601_2k-16xx.csv:                1176 failed

    2023-08-10 - v.40pre2 - reimplementation of chance collecting
        Score of 0.26 vs TideEval 0.40:                 15 - 23 - 42  -> +3.5 comp. to 0.30
        Score of SF14.1/0ply vs. 0.40:                  73 -  2 - 5   -> +3 (!)
        Score of SF14.1/4ply/1600 vs. 0.40:            325 - 25.- 49. -> -4
        Score of *SF11-64/0ply vs TideEval 0.40:        77 -  0 - 3   -> +2 (!)
        Score of *SF11-64/4ply/1600 vs TideEval 0.40:  333 - 16 - 51  -> +17.5 (!)

    2023-08-10 - v.41 - reimpl. ok now + activation of setEvalsForBlockingHere()
        Score of 0.26 vs TideEval:                      16 - 24 - 40  -> -1.5 (!) comp. to v0.40
        Score of SF14.1/0ply vs. TideEval:              76 -  2 - 2   -> -3
        Score of SF14.1/4ply/1600 vs. TideEval:        315 - 25.- 59.  -> +10 (!)
                                                       313 - 34.- 52.  -> +7.5
        Score of *SF11-64/0ply vs TideEval:             77 -  0 - 3   -> =
        Score of *SF11-64/4ply/1600 vs TideEval:       345 - 17.- 37. -> -11.
                                                       351 - 19 - 30  -> -16. (!)
                                                       343 - 15 - 42  -> -10.
                                                  avg. 346.- 17 - 36.
        lichess_db_puzzle_230601_410-499-mateIn1.csv:         387 failed -> +10 comp. to 0.30
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:      492 failed -> +9
        lichess_db_puzzle_230601_2k-9xx.csv:                  675 failed -> +7
        lichess_db_puzzle_230601_2k-16xx.csv:                1168 failed -> +8

    2023-08-10 - v.42 + hanging pieces behind kings
        Score of 0.26 vs TideEval:                      14 - 26 - 40  -> +1 comp. to v0.41
        Score of SF14.1/0ply vs. TideEval:              76 -  2 - 2   -> =
        Score of SF14.1/4ply/1600 vs. TideEval:        313 - xx - 52.  ->
        Score of *SF11-64/0ply vs TideEval:             77 -  0 - 3   -> =
        Score of *SF11-64/4ply/1600 vs TideEval:       339.- 14.- 46. -> +8.
        lichess_db_puzzle_230601_410-499-mateIn1.csv:         383 failed -> +4 comp. to 0.41
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:      494 failed -> -2
        lichess_db_puzzle_230601_2k-9xx.csv:                  679 failed -> -4

    2023-08-10 - v.42b + hanging pieces behind kings, but max benefit setEvalsForBlockingHere
        Score of 0.26 vs TideEval:                      17 - 26 - 37  -> -4.5 comp. to v0.43(!)
        Score of SF14.1/0ply vs. TideEval:              73 -  1 - 6   -> = +1
        Score of SF14.1/4ply/1600 vs. TideEval:        329.- 24 - 46.-> -12 (!! d.h. +12 für setEvalsForBlockingHere in v43 unten)
        Score of *SF11-64/0ply vs TideEval:             80 -  0 - 0   -> = -3
        Score of *SF11-64/4ply/1600 vs TideEval:       340.- 17.- 42. -> +6.

    2023-08-10 - v.42c + hanging pieces behind kings, but NO benefit setEvalsForBlockingHere
        Score of 0.26 vs TideEval:                      12 - 29 - 39  -> +3.5 comp. to v0.42b
        Score of SF14.1/0ply vs. TideEval:              74 -  2 - 4   -> -0.5
        Score of SF14.1/4ply/1600 vs. TideEval:        313 - 28 - 59  -> +14
        Score of *SF11-64/0ply vs TideEval:             77 -  0 - 3   -> +3
        Score of *SF11-64/4ply/1600 vs TideEval:       346.- 15.- 38  -> -5
        lichess_db_puzzle_230601_410-499-mateIn1.csv:         385 failed ->
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:      494 failed ->
        lichess_db_puzzle_230601_2k-9xx.csv:                  675 failed ->

    2023-08-10 - v.43  still hanging pieces + corrected and reduced setEvalsForBlockingHere to exclude the piece with contribution itself + more
        Score of 0.26 vs TideEval:                      14 - 27 - 39  -> -1 comp. to v0.42c
        Score of SF14.1/0ply vs. TideEval:              73 -  2 - 5   -> +1
        Score of SF14.1/4ply/1600 vs. TideEval:        317 - 29.- 53. -> = -5
        Score of *SF11-64/0ply vs TideEval:             78 -  0 - 2   -> -1
        Score of *SF11-64/4ply/1600 vs TideEval:       347 - 19. - 33.  -> -3
        lichess_db_puzzle_230601_410-499-mateIn1.csv:         383 failed -> +2
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:      496 failed -> -2
        lichess_db_puzzle_230601_2k-9xx.csv:                  675 failed -> =
        lichess_db_puzzle_230601_2k-16xx.csv:                x1168 failed ->

    2023-08-10 - v.44a - small improvements here and there (e.g. kings attacking+defending helpless pieces :-)
        Score of 0.26 vs TideEval:                      17 - 27 - 36  -> -3  comp. to v0.43
        Score of SF14.1/0ply vs. TideEval:              78 -  0 - 2   -> -4 (!) , -7.5
        Score of SF14.1/4ply/1600 vs. TideEval:        320.- 26.- 51  -> -3
        Score of *SF11-64/0ply vs TideEval:             79 -  0 - 1   -> -1
        Score of *SF11-64/4ply/1600 vs TideEval:       345.- 16. - 38 -> +2.5  , +5.5

    2023-08-10 - v.44c - fixed very old bug: pawn 2 sq move error after sq1 is freed -> much less "*** Test" Errors in FinalChessBoardEvalTest"
        Score of 0.26 vs TideEval:                      18 - 27 - 35  -> -1  comp. to v0.44a
        Score of SF14.1/0ply vs. TideEval:              78 -  0 - 2   -> -4 (!) , -7.5
        Score of SF14.1/4ply/1600 vs. TideEval:        318 - 35 - 47  -> -2
        Score of *SF11-64/0ply vs TideEval:             79 -  0 - 1   -> -1
        Score of *SF11-64/4ply/1600 vs TideEval:       340- 16. - 43. -> +5 / +2.5
        lichess_db_puzzle_230601_410-499-mateIn1.csv:         374 failed -> +9 comp to v0.43
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:      522 failed -> -26
        lichess_db_puzzle_230601_2k-9xx.csv:                  677 failed -> -2
        lichess_db_puzzle_230601_2k-16xx.csv:                1160 failed -> +8

    2023-08-10 - v.44d - little extra move pawn forward motivation
        Score of 0.26 vs TideEval:                      18 - 26 - 36  -> +0.5  comp. to v0.44c
        Score of SF14.1/0ply vs. TideEval:              75 -  0 - 5   -> +3 (!) , -7.5
        Score of SF14.1/4ply/1600 vs. TideEval:        316.- 27.- 56  -> -8
        Score of *SF11-64/0ply vs TideEval:             80 -  0 - 0   -> -1
        Score of *SF11-64/4ply/1600 vs TideEval:       340 - 23. - 36. -> -3.5

    2023-08-10 - v.44d - little extra move pawn forward motivation + fee for direct pawn doubeling
        Score of 0.26 vs TideEval:                      20 - 27 - 33  -> -2.5  comp. to v0.44c
        Score of SF14.1/0ply vs. TideEval:              75 -  0 - 5   -> =
        Score of SF14.1/4ply/1600 vs. TideEval:        312 - 30 - 58  -> +3
        Score of *SF11-64/0ply vs TideEval:             80 -  0 - 0   -> =
        Score of *SF11-64/4ply/1600 vs TideEval:       343.- 19 - 37. -> -1
        lichess_db_puzzle_230601_410-499-mateIn1.csv:         372 failed -> +2 comp to v0.44c
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:      524 failed -> -2
        lichess_db_puzzle_230601_2k-9xx.csv:                  675 failed -> +2

    2023-08-10 - v.44f - increase motivation for threatened pieaces to move away -relEval>>3  (from >>4)
        Score of 0.26 vs TideEval:                      16 - 26 - 38  -> +4.5  comp. to v0.44d
        Score of SF14.1/0ply vs. TideEval:              76 -  0 - 4   -> -1
        Score of SF14.1/4ply/1600 vs. TideEval:        312 - 35 - 53  -> -2.5
        Score of *SF11-64/0ply vs TideEval:             80 -  0 - 0   -> =
        Score of *SF11-64/4ply/1600 vs TideEval:       331 - 22 - 47  -> +11
        lichess_db_puzzle_230601_410-499-mateIn1.csv:         372 failed -> = comp to v0.44d
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:      526 failed -> -2
        lichess_db_puzzle_230601_2k-9xx.csv:                  675 failed -> =

    2023-08-10 - v.44g - increase motivation for threatened pieaces to move away more: -relEval>>2  (from >>3 / >>4)
        Score of 0.26 vs TideEval:                      20 - 22 - 38  -> -2  comp. to v0.44f
        Score of SF14.1/0ply vs. TideEval:              75 -  0 - 5   -> +1
        Score of SF14.1/4ply/1600 vs. TideEval (4x400) 311 - 35.- 53. -> +2
        Score of *SF11-64/0ply vs TideEval:             79 -  0 - 1   -> +1 (finally again...)
        Score of *SF11-64/4ply/1600 vs TideEval:       344 - 17 - 39 -> -10
        lichess_db_puzzle_230601_410-499-mateIn1.csv:         370 failed -> +2 comp to v0.44f
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:      533 failed -> -7
        lichess_db_puzzle_230601_2k-9xx.csv:                  675 failed ->
        => not used
    2023-08-10 - v.44h - reduce king area benefits
        Score of 0.26 vs TideEval:                      11 - 34 - 35  -> +1  comp. to v0.44f
        Score of SF14.1/0ply vs. TideEval:              74 -  1 - 5   -> +1.5
        Score of SF14.1/4ply/1600 vs. TideEval:        314.- 41 - 44. -> -5
        Score of *SF11-64/0ply vs TideEval:             79 -  0 - 1   -> = +1
        Score of *SF11-64/4ply/1600 vs TideEval:       335.- 28 - 36.  -> -5
        lichess_db_puzzle_230601_410-499-mateIn1.csv:         426 failed -> -56 comp to v0.44f
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:      550 failed -> -24
        lichess_db_puzzle_230601_2k-9xx.csv:                  718 failed -> -43
        => not used 
    
    2023-08-10 - v.44i2 - changed king area benefits a little
        Score of 0.26 vs TideEval:                      13 - 35 - 32  -> -1.5   comp. to v0.44f
        Score of SF14.1/0ply vs. TideEval:              74 -  3 - 3   -> +0.5
        Score of SF14.1/4ply/1600 vs. TideEval: i1!:   307 - 43.- 53. -> +2.5
        Score of *SF11-64/0ply vs TideEval:             80 -  0 - 0   -> =
        Score of *SF11-64/4ply/1600 vs TideEval:       342 - 19.- 38.  -> -12.
        lichess_db_puzzle_230601_410-499-mateIn1.csv:         364 failed -> +8 comp to v0.44f
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:      533 failed -> -7
        lichess_db_puzzle_230601_2k-9xx.csv:                  668 failed -> +7

    2023-08-10 - v.44i3
        Score of 0.26 vs TideEval:                      19 - 27 - 34  -> -2   comp. to v0.44i2
        Score of SF14.1/0ply vs. TideEval:              74 -  1 - 5   -> +1
        Score of SF14.1/4ply/1600 vs. TideEval:        301 - 42 - 57  -> +7.5
        Score of *SF11-64/0ply vs TideEval:             78 -  0 - 2   -> +2
        Score of *SF11-64/4ply/1600 vs TideEval:       343.- 20 - 38. -> -2.
        lichess_db_puzzle_230601_410-499-mateIn1.csv:         428 failed -> -46 comp to v0.44i2
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:      543 failed -> -17
        lichess_db_puzzle_230601_2k-9xx.csv:                  668 failed -> +7
        (NOT used, understandably not beneficial:  LowTide max effect on king area benefits all belonging to the same target)

    2023-08-10 - v.44i4 i3 + 5%
        Score of 0.26 vs TideEval:                      18 - 31 - 31  -> -3   comp. to v0.44i2
        Score of SF14.1/0ply vs. TideEval:              77 -  0 - 3   -> -1.5
        Score of SF14.1/4ply/1600 vs. TideEval:        310.- 33 - 57. -> +1
        Score of *SF11-64/0ply vs TideEval:             80 -  0 - 0   -> =
        Score of *SF11-64/4ply/1600 vs TideEval:       350.- 15 - 34. -> -6
        lichess_db_puzzle_230601_410-499-mateIn1.csv:         359 failed -> +5 comp to v0.44i2
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:      534 failed -> -1
        lichess_db_puzzle_230601_2k-9xx.csv:                  666 failed -> +5

    2023-08-10 - v.44j - calc getKingAreaBenefit for both kings, not just attacking the opponent king
        Score of 0.26 vs TideEval:                      21 - 15 - 44  -> +2   comp. to v0.44i2
        Score of SF14.1/0ply vs. TideEval:              79 -  0 - 1   -> -3.5
        Score of SF14.1/4ply/1600 vs. TideEval:        318 - 26 - 56  -> -4
        Score of *SF11-64/0ply vs TideEval:             80 -  0 - 0   -> =
        Score of *SF11-64/4ply/1600 vs TideEval:       345 - 12 - 43  -> +1
        lichess_db_puzzle_230601_410-499-mateIn1.csv:         390 failed -> -26 comp to v0.44i2
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:      653 failed -> -122
        lichess_db_puzzle_230601_2k-9xx.csv:                  690 failed -> -22

    2023-08-10 - v.44j2 - j adapted for defence cases
        Score of 0.26 vs TideEval:                      19 - 23 - 38  -> -1.5   comp. to v0.44i2
        Score of SF14.1/0ply vs. TideEval:              73 -  0 - 7   -> +2.5
        Score of SF14.1/4ply/1600 vs. TideEval:        315.- 29. - 55 -> -3
        Score of *SF11-64/0ply vs TideEval:             78 -  0 - 2   -> +2 (!)
        Score of *SF11-64/4ply/1600 vs TideEval:       347.- 15.- 37.  -> -1.5
        lichess_db_puzzle_230601_410-499-mateIn1.csv:         377 failed -> -13 comp to v0.44i2
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:      575 failed -> -42
        lichess_db_puzzle_230601_2k-9xx.csv:                  686 failed -> -18

    2023-08-10 - v.44j3 - j adapted for defence cases
        Score of 0.26 vs TideEval:                      19 - 23 - 38  -> = comp. to v0.44j2
        Score of SF14.1/0ply vs. TideEval:              77 -  1 - 2   -> -0.5
        Score of SF14.1/4ply/1600 vs. TideEval:        306 - 37 - 57 -> +6
        Score of *SF11-64/0ply vs TideEval:             79 -  0 - 1   -> -1
        Score of *SF11-64/4ply/1600 vs TideEval:       341.- 18 - 40.  -> + 4.5
        lichess_db_puzzle_230601_410-499-mateIn1.csv:         376 failed -> +1 comp to v0.44j2
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:      576 failed -> -1
        lichess_db_puzzle_230601_2k-9xx.csv:                  687 failed -> -1

    2023-08-10 - v.44k - correction of close future attacking benefits (no clashContrib after prev. additional attacker already got the same amount + relEval-based benefit)
        Score of 0.26 vs TideEval:                      18 - 24 - 38  -> +0.5  comp. to v0.44j3
        Score of SF14.1/0ply vs. TideEval:              77 -  0 - 3   -> +0.5
        Score of SF14.1/4ply/1600 vs. TideEval:        323.- 29 - 47. -> -14
        Score of *SF11-64/0ply vs TideEval:             79 -  0 - 1   -> =
        Score of *SF11-64/4ply/1600 vs TideEval:       351.- 15 - 33. -> -8.5
        lichess_db_puzzle_230601_410-499-mateIn1.csv:         370 failed ->-6  comp to v0.44j3
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:      555 failed -> +21
        lichess_db_puzzle_230601_2k-9xx.csv:                  684 failed -> +3

    2023-08-10 - v.44k3 - correction of close future attacking benefits (no clashContrib after prev. additional attacker already got the same amount + relEval-based benefit)
        Score of 0.26 vs TideEval:                      17 - 26 - 37  -> +0.5  comp. to v0.44j3
        Score of SF14.1/0ply vs. TideEval:              74 -  2 - 4   -> +2.5
        Score of SF14.1/4ply/1600 vs. TideEval:        325 - 22.- 52. -> -12
        Score of *SF11-64/0ply vs TideEval:             77 -  0 - 3   -> +2
        Score of *SF11-64/4ply/1600 vs TideEval:       346 - 13.- 40.  -> -2
        lichess_db_puzzle_230601_410-499-mateIn1.csv:         421 failed -> -- comp to v0.44j3
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:      571 failed -> =
        lichess_db_puzzle_230601_2k-9xx.csv:                  712 failed -> -

    2023-08-10 - v.44l - more benefit to pawns that seemt to be able to move straight to promotion - and a bit less to the others
        Score of 0.26 vs TideEval:                      17 - 34 - 29  -> -4  comp. to v0.44k3
        Score of SF14.1/0ply vs. TideEval:              73 -  2 - 5   -> +1
        Score of SF14.1/4ply/1600 vs. TideEval:        308.- 41 - 50. -> +7
        Score of *SF11-64/0ply vs TideEval:             79 -  0 - 1   -> -2
        Score of *SF11-64/4ply/1600 vs TideEval:       338.- 27 - 34.  > +1
        lichess_db_puzzle_230601_410-499-mateIn1.csv:         412 failed -> -36 comp to v0.44j3(!)
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:      530 failed -> +25
        lichess_db_puzzle_230601_2k-9xx.csv:                  676 failed -> +11

    2023-08-10 - v.44m - experiment: postpone first king attacking by 1 future level
        Score of 0.26 vs TideEval:                      17 - 30 - 33  -> +2   comp. to v0.44k3
        Score of SF14.1/0ply vs. TideEval:              74 -  2 - 4   -> -1
        Score of SF14.1/4ply/1600 vs. TideEval:        299.- 55. - 45 -> +2
        Score of *SF11-64/0ply vs TideEval:             77 -  0 - 3   -> +2
        Score of *SF11-64/4ply/1600 vs TideEval:       345.- 25 - 29.  > -6
        lichess_db_puzzle_230601_410-499-mateIn1.csv:         431 failed -> -19 comp to v0.44j3(!)
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:      534 failed -> -4
        lichess_db_puzzle_230601_2k-9xx.csv:                  686 failed -> -10

    2023-08-25 - v.45 - isChecking flag for indirect moving away check moves
        Score of 0.26 vs TideEval:                      13 - 35 - 32  -> +1.5   comp. to v0.44m
        Score of SF14.1/0ply vs. TideEval:              73 -  2 - 5   -> +1
        Score of SF14.1/4ply/1600 vs. TideEval:        304 - 49.- 46. -> -1.5
        Score of *SF11-64/0ply vs TideEval:             79 -  0 - 1   -> -2
        Score of *SF11-64/4ply/1600 vs TideEval:       340 - 31 - 29   > +2.5

    2023-08-25 - v.45a - same + little extra EVAL-score for those moves
        Score of 0.26 vs TideEval:                      12 - 36 - 32  -> +2  comp. to v0.44m
        Score of SF14.1/0ply vs. TideEval:              73 -  2 - 5   -> +1
        Score of SF14.1/4ply/1600 vs. TideEval:        295.- 56 - 48. -> +1
        Score of *SF11-64/0ply vs TideEval:             79 -  0 - 1   -> -2
        Score of *SF11-64/4ply/1600 vs TideEval:       336.- 34.- 29  -> +2
        lichess_db_puzzle_230601_410-499-mateIn1.csv:         428 failed -> +3  comp to v0.44m

    2023-08-25 - v.46 - now thinks about likely2Bkilled, this changes Nogo calculation + Conditions instead of NoGos if target square is only slightly blocked by 1 opponent
        Score of 0.26 vs TideEval:                      27 - 30 - 23  -> -11.   comp. to v0.45a
        Score of SF14.1/0ply vs. TideEval:              76 -  0 - 4   -> -2
        Score of SF14.1/4ply/1600 vs. TideEval:        325.- 36 - 38. -> -20 (!)
        Score of *SF11-64/0ply vs TideEval:             78 -  0 - 2   -> +1
        Score of *SF11-64/4ply/1600 vs TideEval:       350.- 22.- 27  -> -8
        lichess_db_puzzle_230601_410-499-mateIn1.csv:         436 failed -> -8  comp to v0.45a
                                        AvoidMateIn1:        1996 failed  ca. -180 (zu v.29)
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:      596 failed -> -62
        lichess_db_puzzle_230601_2k-9xx.csv:                  719 failed -> -33

    2023-08-25 - v.46a - now thinks about likely2Bkilled, this changes Nogo calculation - but here without change of Conditions instead of NoGos
        Score of 0.26 vs TideEval:                      14 - 40 - 26  -> -4 comp. to v0.45a
        Score of SF14.1/0ply vs. TideEval:              76 -  1 - 3   -> -2.5
        Score of SF14.1/4ply/1600 vs. TideEval:        321.- 46 - 32. -> -15
        Score of *SF11-64/0ply vs TideEval:             80 -  0 - 0   -> -1
        Score of *SF11-64/4ply/1600 vs TideEval:       346.- 28.- 25  -> -7
        lichess_db_puzzle_230601_410-499-mateIn1.csv:         419 failed -> +9  comp to v0.45a
                                        AvoidMateIn1:        2013 failed  ca. -200 (zu v.29)
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:      554 failed -> -20 (v.44m)
        lichess_db_puzzle_230601_2k-9xx.csv:                  733 failed -> -51

    2023-08-25 - v.46b - without "calcClash... recalculated straight moving pawn could also trigger a 2nd row piece"
        Score of 0.26 vs TideEval:                      15 - 41 - 24  -> -5.5 comp. to v0.45a
        Score of SF14.1/0ply vs. TideEval:              75 -  1 - 4   -> -1.5
        Score of SF14.1/4ply/1600 vs. TideEval:        312 - 50.- 37. -> -13

    2023-08-25 - v.46c - without improved "fulfilledConditionsCouldMakeDistIs1()" and without using killedReasonablySure()
        Score of 0.26 vs TideEval:                      11 - 39 - 30  -> -0.5  comp. to v0.45a
        Score of SF14.1/0ply vs. TideEval:              77 -  0 - 3   -> -3
        Score of SF14.1/4ply/1600 vs. TideEval:        306.- 47.- 46  -> -8
        Score of *SF11-64/0ply vs TideEval:             78 -  0 - 2   -> +1
        Score of *SF11-64/4ply/1600 vs TideEval:       339.- 31.- 29  -> -1.5
        lichess_db_puzzle_230601_410-499-mateIn1.csv:         421 failed -> +7    comp to v0.45a
                                        AvoidMateIn1:        1989 failed  ?
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:      524 failed -> +10  (v.44m)
        lichess_db_puzzle_230601_2k-9xx.csv:                  696 failed -> -10  (v.44m)
        => everything undone and more or less equal again. hmmm

    2023-08-26 - v.46d - correction of old_eval reg. 2nd row / moreWhites
        Score of 0.26 vs TideEval:                      10 - 41 - 29  -> =  comp. to v0.45a
        Score of SF14.1/0ply vs. TideEval:              77 -  0 - 3   -> -3
        Score of SF14.1/4ply/1600 vs. TideEval:        313.- 50 - 37. -> -15
        Score of *SF11-64/0ply vs TideEval:             79 -  0 - 1   -> -1
        Score of *SF11-64/4ply/1600 vs TideEval:       338.- 32.- 29  -> -1
        lichess_db_puzzle_230601_410-499-mateIn1.csv:         420 failed -> +1    comp to v0.46c
                                        AvoidMateIn1:        1987 failed -> +2
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:      523 failed -> +1
        lichess_db_puzzle_230601_2k-9xx.csv:                  696 failed -> =

    2023-08-26 - v.45e - (named 45, as all 46 functions are inactive anyway...) correction of call to calcClashResultExcludingOne in futireClashEval, concerning moreWhites / 2nd row etc
        Score of 0.26 vs TideEval:                       9 - 43 - 28  -> =  comp. to v0.45a
        Score of SF14.1/0ply vs. TideEval:              77 -  0 - 3   -> -3
        Score of SF14.1/4ply/1600 vs. TideEval:        303 - 53.- 43. -> -6
        Score of *SF11-64/0ply vs TideEval:             79 -  0 - 1   -> -1
        Score of *SF11-64/4ply/1600 vs TideEval:       332 - 34 - 34  -> +5
        lichess_db_puzzle_230601_410-499-mateIn1.csv:      420 failed -> +1   comp to v0.46c
                                        AvoidMateIn1:     1987 failed -> +2
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:   523 failed -> +1

    2023-08-26 - v.45f - 45e + corrected fulfilledConditionsCouldMakeDistIs1()
        Score of 0.26 vs TideEval:                       9 - 43 - 28  -> =  comp. to v0.45a
        Score of SF14.1/0ply vs. TideEval:              77 -  0 - 3   -> -3
        Score of SF14.1/4ply/1600 vs. TideEval:        305.- 52.- 42  -> -8
        Score of *SF11-64/0ply vs TideEval:             79 -  0 - 1   -> -1
        Score of *SF11-64/4ply/1600 vs TideEval:       338.- 30 - 31. -> +0.5
        lichess_db_puzzle_230601_410-499-mateIn1.csv:      420 failed -> +1    comp to v0.46c
                                        AvoidMateIn1:     1986 failed -> +3
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:   523 failed -> +1
        lichess_db_puzzle_230601_2k-9xx.csv:               695 failed -> +1

    2023-08-26 - v.46i + less future clash benefit + use (above unsuccessful) check for leaving squares uncoverd for an immediate opponent move at least to give out contributions
        Score of 0.26 vs TideEval:                      18 - 28 - 34  -> -1.5  comp. to v0.45f
        Score of SF14.1/0ply vs. TideEval:              77 -  0 - 3   -> =
        Score of SF14.1/4ply/1600 vs. TideEval:        305.- 52.- 42  -> -8
        Score of *SF11-64/0ply vs TideEval:             79 -  0 - 1   -> =
        Score of *SF11-64/4ply/1600 vs TideEval:       338.- 30 - 31. -> +0.5
        lichess_db_puzzle_230601_410-499-mateIn1.csv:      420 failed -> +1  comp to v0.45f
                                        AvoidMateIn1:     1986 failed -> +3
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:   523 failed -> +1
        lichess_db_puzzle_230601_2k-9xx.csv:               695 failed -> +1

    2023-08-26 - v.46l + use (above unsuccessful) check for leaving squares uncoverd for an immediate opponent move at least to give out contributions 100/200 for his possible check-giving moves
        Score of 0.26 vs TideEval:                       9 - 40 - 31  -> +1.5  comp. to v0.45f
        Score of SF14.1/0ply vs. TideEval:              77 -  0 - 3   -> =
        Score of SF14.1/4ply/1600 vs. TideEval:        309.- 44.- 46  -> =
        Score of *SF11-64/0ply vs TideEval:             79 -  0 - 1   -> =
        Score of *SF11-64/4ply/1600 vs TideEval:       335.- 35.- 29 -> =

    2023-08-26 - v.46m + like 46l, but a little contributions for non-checking squares conquerable by opponent.
        Score of 0.26 vs TideEval:                                          comp. to v0.45f
        Score of SF14.1/0ply vs. TideEval:              74 -  1 - 5   -> +2.5
        Score of SF14.1/4ply/1600 vs. TideEval:        311.- 45 - 43. -> -2
        Score of *SF11-64/0ply vs TideEval:             78 -  1 - 1   -> +0.5
        Score of *SF11-64/4ply/1600 vs TideEval:       336.- 35 - 28. -> -1
        lichess_db_puzzle_230601_410-499-mateIn1.csv:      425 failed -> -5  comp to v0.45f
                                        AvoidMateIn1:     1986 failed ->
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:   527 failed -> -4
        lichess_db_puzzle_230601_2k-9xx.csv:               695 failed ->

    2023-08-26 - v.46n + like 46m but less benefit for future attackers fl>=2
        Score of 0.26 vs TideEval:                                          comp. to v0.45f
        Score of SF14.1/0ply vs. TideEval:              74 -  2 - 4   -> +2
        Score of SF14.1/4ply/1600 vs. TideEval:        301 - 51.- 45. -> +4
        Score of *SF11-64/0ply vs TideEval:             79 -  1 - 0   -> -0.5
        Score of *SF11-64/4ply/1600 vs TideEval:       336.- 35 - 28. ->
        lichess_db_puzzle_230601_410-499-mateIn1.csv:      426 failed -> -6  comp to v0.45f
                                        AvoidMateIn1:     1986 failed ->
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:   525 failed -> -2
        lichess_db_puzzle_230601_2k-9xx.csv:               695 failed ->

    2023-08-26 - v.46o + like 46n but no extra avoid conquer contrib if square is occupied
        Score of 0.26 vs TideEval:                                          comp. to v0.45f
        Score of SF14.1/0ply vs. TideEval:              73 -  1 - 6   -> +3.5
        Score of SF14.1/4ply/1600 vs. TideEval:        311.- 41.- 47  -> -0.5
        Score of *SF11-64/0ply vs TideEval:             78 -  1 - 1   -> +0.5
        Score of *SF11-64/4ply/1600 vs TideEval:       333.- 38 - 28. ->
        lichess_db_puzzle_230601_410-499-mateIn1.csv:      426 failed -> -6  comp to v0.45f
                                        AvoidMateIn1:     1987 failed -> -1
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:   524 failed -> -1
        lichess_db_puzzle_230601_2k-9xx.csv:               696 failed -> -1


    2023-08-26 - v.46p corrects hanging piece behing king
        Score of 0.26 vs TideEval:                      14 - 42 - 24  -> -5,5 (comp. to 46k)
        Score of SF14.1/0ply vs. TideEval:              72 -  1 - 7   -> +1 comp. to v0.45o
        Score of SF14.1/4ply/1600 vs. TideEval:        317.- 43 - 45. -> -4.5
        Score of *SF11-64/0ply vs TideEval:             78 -  1 - 1   -> =
        Score of *SF11-64/4ply/1600 vs TideEval:       338.- 31.- 30. -> -1.5
        lichess_db_puzzle_230601_410-499-mateIn1.csv:      428 failed -> -2   comp to v0.45o
                                        AvoidMateIn1:     1987 failed -> =
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:   530 failed -> -6
        lichess_db_puzzle_230601_2k-9xx.csv:               679 failed -> +17

    2023-08-26 - v.46r improves addBenefitToBlockers, also count already covering the hopping point + contribution for these
        Score of 0.26 vs TideEval:                 R    14 - 44 - 22  -> -6,5 (comp. to 46k)
        Score of SF14.1/0ply vs. TideEval:         R    74 -  1 - 5   -> +4 comp. to v0.45o
        Score of SF14.1/4ply/1600 vs. TideEval:        310 - 50.- 39. -> -3.5
        Score of *SF11-64/0ply vs TideEval:             77 -  2 - 1   -> +0.5
        Score of *SF11-64/4ply/1600 vs TideEval:       325 - 37 - 38  -> +9

    2023-08-26 - v.46s - reduce benefit fir fl>1 for moving away / enabling other benfits cases
        Score of 0.26 vs TideEval:                      14 - 44 - 22  -> X-6,5   comp. to v0.45r
        Score of SF14.1/0ply vs. TideEval:              73 -  1 - 6   -> +1
        Score of SF14.1/4ply/1600 vs. TideEval:        310.- 51 - 39. -> X-3.5
        Score of *SF11-64/0ply vs TideEval:             80 -  0 - 0   -> -2
        Score of *SF11-64/4ply/1600 vs TideEval:       325 - 37 - 38  -> X+9
        lichess_db_puzzle_230601_2k-9xx.csv:               701 failed -> -22

    2023-08-26 - v.46t5 - count blockers for hanging piece behind king and change benefit + benefit blockers
        Score of 0.26 vs TideEval:                      19 - 39 - 22  -> -2.5   comp. to v0.45r
        Score of SF14.1/0ply vs. TideEval:              79 -  0 - 1   -> -4.5 (!)
        Score of SF14.1/4ply/1600 vs. TideEval:        313 - 50 - 37  -> -3
        Score of *SF11-64/0ply vs TideEval:             78 -  1 - 1   -> -0.5
        Score of *SF11-64/4ply/1600 vs TideEval:       340 - 34.- 26  -> -13.5 (!)
        lichess_db_puzzle_230601_410-499-mateIn1.csv:      400 failed -> +28   comp to v0.45p
                                        AvoidMateIn1:     1962 failed -> +25
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:   453 failed -> +77
        lichess_db_puzzle_230601_2k-9xx.csv:               675 failed -> +4

    2023-08-26 - v.46t6 - like t5 + switch noGo for isKillable on again (only for empty squares)
        Score of 0.26 vs TideEval:                      17 - 41 - 22  -> -1.5   comp. to v0.45r
        Score of SF14.1/0ply vs. TideEval:              76 -  1 - 3   -> -2
        Score of SF14.1/4ply/1600 vs. TideEval:        328 - 41 - 31  -> -12.5  (!)
        Score of *SF11-64/0ply vs TideEval:             79 -  0 - 1   -> -1
        Score of *SF11-64/4ply/1600 vs TideEval:       358.- 21 - 20. -> -14 (!)
    => not used for now. idea is interesting, but seems to induce other problems.

    2023-08-26 - v.46t7 - like t5 + less reduction for higher fl
        Score of 0.26 vs TideEval:                      16 - 43 - 21  -> -1.5   comp. to v0.45r
        Score of SF14.1/0ply vs. TideEval:              80 -  0 - 0   -> -2.5
        Score of SF14.1/4ply/1600 vs. TideEval:        310.- 52 - 37.  -> -1
        Score of *SF11-64/0ply vs TideEval:             77 -  0 - 3   -> +0.5
        Score of *SF11-64/4ply/1600 vs TideEval:       339.- 33.- 27 -> -13 (!)
        lichess_db_puzzle_230601_410-499-mateIn1.csv:      403 failed -> +25   comp to v0.45p

    2023-08-26 - v.46t8 - like t5 + now completely without reduction for higher fl
        Score of 0.26 vs TideEval:                      17 - 40 - 23  -> -1   comp. to v0.45r
        Score of SF14.1/0ply vs. TideEval:              78 -  0 - 2   -> -2.5
        Score of SF14.1/4ply/1600 vs. TideEval:        306.- 51 - 42. -> +3
        Score of *SF11-64/0ply vs TideEval:             80 -  0 - 0   -> -2
        Score of *SF11-64/4ply/1600 vs TideEval:       344.- 28 - 27. -> -15 (!)
        lichess_db_puzzle_230601_410-499-mateIn1.csv:      405 failed -> +23   comp to v0.45p
                                        AvoidMateIn1:     1955 failed -> +32
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:   455 failed -> +75
        lichess_db_puzzle_230601_2k-9xx.csv:               687 failed -> -8

    2023-08-26 - v.46u2 - like t8 + repaired blockinf Benefits for straight pawns (not attacking)
        Score of 0.26 vs TideEval:                      17 - 40 - 23  -> -1   comp. to v0.45r
        Score of SF14.1/0ply vs. TideEval:              76 -  0 - 4   -> -.5
        Score of SF14.1/4ply/1600 vs. TideEval:        305 - 52.- 42. -> +4
        Score of *SF11-64/0ply vs TideEval:             78 -  0 - 2   -> =
        Score of *SF11-64/4ply/1600 vs TideEval:       341 - 34.- 24. -> -15 (!)
        lichess_db_puzzle_230601_410-499-mateIn1.csv:      394 failed -> +34   comp to v0.45p
                                        AvoidMateIn1:     1961 failed -> +26
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:   453 failed -> +77
        lichess_db_puzzle_230601_2k-9xx.csv:               689 failed -> -10

    2023-08-26 - v.46u3 - like u2 + more fixes in blocking Benefits
        Score of 0.26 vs TideEval:                      14 - 39 - 27  -> +2.5 comp. to v0.45r:  14 - 44 - 22
        Score of SF14.1/0ply vs. TideEval:              78 -  0 - 2   -> -3.5                   74 -  1 - 5
        Score of SF14.1/4ply/1600 vs. TideEval:        297 - 65 - 38  -> +7                    310 - 50.- 39.
        Score of *SF11-64/0ply vs TideEval:             80 -  0 - 0   -> -2                     77 -  2 - 1
        Score of *SF11-64/4ply/1600 vs TideEval:       339 - 38.- 22. -> -8                    325 - 37 - 38
        lichess_db_puzzle_230601_410-499-mateIn1.csv:      427 failed -> +1   comp to v0.45p:   428
                                        AvoidMateIn1:     1968 failed -> +19                   1987
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:   546 failed -> -16                    530
        lichess_db_puzzle_230601_2k-9xx.csv:               721 failed -> -42                    679

    2023-08-30 v46u4: u3 with less blocker-benefit when >1 blockers
        Score of 0.26 vs TideEval:                      15 - 39 - 26  -> +1.5 comp. to v0.45r:  14 - 44 - 22
        Score of SF14.1/0ply vs. TideEval:              78 -  0 - 2   -> -3.5                   74 -  1 - 5
        Score of SF14.1/4ply/1600 vs. TideEval:        307.- 58 - 34. -> =                     310 - 50.- 39.
        Score of *SF11-64/0ply vs TideEval:             80 -  0 - 0   -> -2                     77 -  2 - 1
        Score of *SF11-64/4ply/1600 vs TideEval:       337 - 34.- 28. -> -4                    325 - 37 - 38
        -> not used

    2023-08-30 v46u5: u3 with less (*0.75) attacker-benefit when >1 blockers  (*0.5 was a little worse than 0.75, *0.83 also a little worse)
        Score of 0.26 vs TideEval:                      14 - 39 - 27  -> +2.5 comp. to v0.45r:  14 - 44 - 22
        Score of SF14.1/0ply vs. TideEval:              78 -  0 - 2   -> -3.5                   74 -  1 - 5
        Score of SF14.1/4ply/1600 vs. TideEval:        304 - 65 - 31  -> -1                    310 - 50.- 39.
        Score of *SF11-64/0ply vs TideEval:             80 -  0 - 0   -> -2                     77 -  2 - 1
        Score of *SF11-64/4ply/1600 vs TideEval:       325.- 43.- 32 -> -3                     325 - 37 - 38
                                        AvoidMateIn1:     1986 failed -> +1                    1987

    2023-08-30 v46u9: u5 + attackerBenefit at attacking square (not kingpos)
        Score of 0.26 vs TideEval:                      12 - 42 - 26  -> +3    comp. to v0.45r: 14 - 44 - 22
        Score of SF14.1/0ply vs. TideEval:              78 -  0 - 2   -> -3.5                   74 -  1 - 5
        Score of SF14.1/4ply/1600 vs. TideEval:        299 - 60.- 40. -> +6                    310 - 50.- 39.
        Score of *SF11-64/0ply vs TideEval:             80 -  0 - 0   -> -2                     77 -  2 - 1
        Score of *SF11-64/4ply/1600 vs TideEval:       340 - 36.- 23. -> -15 (!)               325 - 37 - 38

    2023-08-30 v46v: u9 + no 1/4*mate bonus for check move with seemingly no opponent moves left in final move decision, because this ruins 3-fold-repetition detection in those cases-
        Score of 0.26 vs TideEval:                      11 - 49 - 20  -> +.5    comp. to v0.45r: 14 - 44 - 22
        Score of SF14.1/0ply vs. TideEval:              78 -  0 - 2   -> -3.5                   74 -  1 - 5
        Score of SF14.1/4ply/1600 vs. TideEval:        298 - 66 - 36  -> +4                    310 - 50.- 39.
        Score of *SF11-64/0ply vs TideEval:             80 -  0 - 0   -> -2                     77 -  2 - 1
        Score of *SF11-64/4ply/1600 vs TideEval:       338.- 37 - 24. -> -13. (!)               325 - 37 - 38
        lichess_db_puzzle_230601_410-499-mateIn1.csv:      467 failed -> +1   comp to v0.45p:   428
                                        AvoidMateIn1:     2010 failed -> +23                   1987
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:   545 failed -> -15                    530
        lichess_db_puzzle_230601_2k-9xx.csv:               715 failed -> -36                    679

    2023-08-30 v46w: v + double contribution exploit benefit :-)
        Score of 0.26 vs TideEval:                      12 - 46 - 22  -> +0.5    comp. to v0.46v
        Score of SF14.1/0ply vs. TideEval:              77 -  0 - 3   -> +1
        Score of SF14.1/4ply/1600 vs. TideEval:        303 - 60 - 37  -> -2
        Score of *SF11-64/0ply vs TideEval:             80 -  0 - 0   -> =
        Score of *SF11-64/4ply/1600 vs TideEval:       338 - 39.- 22. -> -1

    2023-08-30 v46x: w + correction sign-error (!) in king-pin benefit
        Score of 0.26 vs TideEval:                      12 - 42 - 26  -> +2   comp. to v0.46w
        Score of SF14.1/0ply vs. TideEval:              77 -  0 - 3   -> =
        Score of SF14.1/4ply/1600 vs. TideEval:        311 - 56 - 33  -> -6
        Score of *SF11-64/0ply vs TideEval:             80 -  0 - 0   -> =
        Score of *SF11-64/4ply/1600 vs TideEval:       342.- 39 - 18. -> -4
        lichess_db_puzzle_230601_410-499-mateIn1.csv:      473 failed -> -6   comp to v0.46v:
                                        AvoidMateIn1:     2015 failed -> -5
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:   550 failed -> -5
        lichess_db_puzzle_230601_2k-9xx.csv:               706 failed -> +9

    2023-08-30 v46x2: x + less benefit, if pinned piece can take attacker with less loss
        Score of 0.26 vs TideEval:                      14 - 43 - 23  -> -0.5   comp. to v0.46w
        Score of SF14.1/0ply vs. TideEval:              77 -  0 - 3   -> =
        Score of SF14.1/4ply/1600 vs. TideEval:        305.- 64 - 29. -> -4.5
        Score of *SF11-64/0ply vs TideEval:             80 -  0 - 0   -> =
        Score of *SF11-64/4ply/1600 vs TideEval:       336.- 39 - 24. -> +2
        lichess_db_puzzle_230601_410-499-mateIn1.csv:      466 failed -> +1   comp to v0.46v:

    2023-08-30 v46y: x2 + corrected dir-comparison in Abzugschach and attack-through king
        Score of 0.26 vs TideEval:                      16 - 41 - 23  -> -1   comp. to v0.46x2
        Score of SF14.1/0ply vs. TideEval:              77 -  0 - 3   -> =
        Score of SF14.1/4ply/1600 vs. TideEval:        309 - 56.- 34. -> +5
        Score of *SF11-64/0ply vs TideEval:             80 -  0 - 0   -> =
        Score of *SF11-64/4ply/1600 vs TideEval:       335 - 42 - 23  -> =
        lichess_db_puzzle_230601_410-499-mateIn1.csv:      466 failed -> =   comp to v0.46x2:
                                        AvoidMateIn1:     2014 failed -> +1  com to x
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:   548 failed -> +2
        lichess_db_puzzle_230601_2k-9xx.csv:               709 failed -> -3

    2023-09-01 v46z1: y + minor corrections in king area
        Score of 0.26 vs TideEval:                      13 - 39 - 28  -> +4   comp. to v0.46y
        Score of SF14.1/0ply vs. TideEval:              79 -  0 - 1   -> -2
        Score of SF14.1/4ply/1600 vs. TideEval:        303.- 53.- 43 -> +7
        Score of *SF11-64/0ply vs TideEval:             80 -  0 - 0   -> =
        Score of *SF11-64/4ply/1600 vs TideEval:       333 - 31.- 35. -> +7

    2023-09-01 v46z10: y + do not assume magic right triangle works if move hinders a check +  a little less king helps out benefit
        Score of 0.26 vs TideEval:                      13 - 40 - 27  -> -0.5  comp. to v0.46z1
        Score of SF14.1/0ply vs. TideEval:              79 -  1 - 0   -> -0,5
        Score of SF14.1/4ply/1600 vs. TideEval:        299 - 62.- 38. -> =
        Score of *SF11-64/0ply vs TideEval:             80 -  0 - 0   -> =
        Score of *SF11-64/4ply/1600 vs TideEval:       329 - 38.- 32. -> +0.5
        lichess_db_puzzle_230601_410-499-mateIn1.csv:      466 failed -> =   comp to v0.46y
                                        AvoidMateIn1:     2014 failed -> +1  com to x
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:   548 failed -> +2
        lichess_db_puzzle_230601_2k-9xx.csv:               706 failed ->

    2023-09-01 v46z16: z10 + a little less bonus for unbenefitial attacking at d==2
        Score of 0.26 vs TideEval:                      20 - 33 - 27  -> -3.5  comp. to v0.46z10
        Score of SF14.1/0ply vs. TideEval:              74 -  3 - 3   -> +4
        Score of SF14.1/4ply/1600 vs. TideEval:        303 - 58.- 38. -> -2
        Score of *SF11-64/0ply vs TideEval:             79 -  0 - 1   -> +1
        Score of *SF11-64/4ply/1600 vs TideEval:       347 - 25 - 27. -> -11

    2023-09-01 v46z17: z18 + a little less less bonus for unbenefitial attacking at d==2
        Score of 0.26 vs TideEval:                      11 - 42 - 27  -> +4.5   comp. to v0.46z16
        Score of SF14.1/0ply vs. TideEval:              79 -  0 - 1   -> -3.5
        Score of SF14.1/4ply/1600 vs. TideEval:        296 - 66 - 38  -> +3
        Score of *SF11-64/0ply vs TideEval:             79 -  0 - 1   -> =
        Score of *SF11-64/4ply/1600 vs TideEval:       326 - 44.- 29. -> +7.5
        lichess_db_puzzle_230601_410-499-mateIn1.csv:      453 failed -> +13   comp to v0.46z10
                                        AvoidMateIn1:     2012 failed -> +2
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:   540 failed -> +8
        lichess_db_puzzle_230601_2k-9xx.csv:               701 failed -> +5  (comp to 46y)

    2023-09-01 v46z21: + motivate to go to block good moves already from further distances
        Score of 0.26 vs TideEval:                      11 - 42 - 27  -> =   comp. to v0.46z17
        Score of SF14.1/0ply vs. TideEval:              79 -  0 - 1   -> =
        Score of SF14.1/4ply/1600 vs. TideEval:        290 - 63 - 47  -> +7.5
                                                       300 - 56 - 44
        Score of *SF11-64/0ply vs TideEval:             79 -  0 - 1   -> =
        Score of *SF11-64/4ply/1600 vs TideEval:       329 - 33 - 38  -> +3
                                                       336 - 32 - 32

    2023-09-01 v46z21: + motivate to go to block good moves already from further distances
        Score of 0.26 vs TideEval:                      12 - 39 - 29  -> +0.5   comp. to v0.46z21
        Score of SF14.1/0ply vs. TideEval:              76 -  0 - 4   -> +3
        Score of SF14.1/4ply/1600 vs. TideEval:        293 - 64 - 43  -> -0.5
        Score of *SF11-64/0ply vs TideEval:             78 -  1 - 1   -> +0.5
        Score of *SF11-64/4ply/1600 vs TideEval:       331 - 35.- 33. -> -0.5
        lichess_db_puzzle_230601_410-499-mateIn1.csv:      453 failed -> =   comp to v0.46z18
                                        AvoidMateIn1:     2013 failed -> -1
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:   540 failed -> =
        lichess_db_puzzle_230601_2k-9xx.csv:               701 failed -> =


    2023-09-06 v47h: final move selection obeys tempi, resp who's turn it is after a clash
        Score of 0.26 vs TideEval:                  R   15 - 40 - 25  -> -3.5   comp. to v0.46z21
        Score of SF14.1/0ply vs. TideEval:              75 -  1 - 4   -> +0.5
        Score of SF14.1/4ply/1600 vs. TideEval:        300 - 58 - 41. -> -4
        Score of *SF11-64/0ply vs TideEval:             78 -  1 - 1   -> =
        Score of *SF11-64/4ply/1600 vs TideEval:       330 - 37.- 32. -> =
        lichess_db_puzzle_230601_410-499-mateIn1.csv:  R   425 failed -> +28   comp to v0.46z21
                                        AvoidMateIn1:     2004 failed -> +9
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:   550 failed -> -10
        lichess_db_puzzle_230601_2k-9xx.csv:               702 failed -> -1

    2023-09-06 v47o:  more thorough test of straight way to promotion + eliminates incorrect pawn-nogos at dist>4
        Score of 0.26 vs TideEval:                       6 - 53 - 21  -> +2.5   comp. to v0.47h
        Score of SF14.1/0ply vs. TideEval:              77 -  1 - 2   -> -2
        Score of SF14.1/4ply/1600 vs. TideEval:        303.- 76. - 20 -> -12
        Score of *SF11-64/0ply vs TideEval:             78 -  0 - 2   -> +0.5
        Score of *SF11-64/4ply/1600 vs TideEval:       (322 - 58-  20 /200) -> -
    -> not used

    2023-09-06 v47p: like o but only eliminates incorrect pawn-nogos at dist>4
        Score of 0.26 vs TideEval:                       8 - 53 - 19  -> +0.5   comp. to v0.47h
        Score of SF14.1/0ply vs. TideEval:              79 -  0 - 1   -> -3.5
        Score of SF14.1/4ply/1600 vs. TideEval:        294 - 77 - 29  -> -3
        Score of *SF11-64/0ply vs TideEval:             75 -  1 - 4   -> +3
        Score of *SF11-64/4ply/1600 vs TideEval:       339.- 42 - 18. -> -12

    2023-09-06 v47q: like o but only more thorough test of straight way to promotion
        Score of 0.26 vs TideEval:                      13 - 54 - 13  -> -5   comp. to v0.47h
        Score of SF14.1/0ply vs. TideEval:              78 -  0 - 2   -> -2.5
        Score of SF14.1/4ply/1600 vs. TideEval:        293.- 86 - 20  -> -7.5
        Score of *SF11-64/0ply vs TideEval:             77 -  0 - 3   -> +1.5
        Score of *SF11-64/4ply/1600 vs TideEval:       339.- 42 - 18. ->

    2023-09-08 v47t3:
        Score of 0.26 vs TideEval:                   R  11 - 35 - 34  -> +6.5   comp. to v0.47h
        Score of SF14.1/0ply vs. TideEval:              79 -  0 - 1   -> -1
        Score of SF14.1/4ply/1600 vs. TideEval:        294.- 51.- 54  -> +9
        Score of *SF11-64/0ply vs TideEval:             79 -  0 - 1   -> -2
        Score of *SF11-64/4ply/1600 vs TideEval:       336 - 37 - 27  -> -5.5
        lichess_db_puzzle_230601_410-499-mateIn1.csv:  R   477 failed -> -52    comp to v0.47h
                                        AvoidMateIn1:     2015 failed -> -11
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:   565 failed -> -15
        lichess_db_puzzle_230601_2k-9xx.csv:               723 failed -> -11

    2023-09-09 v47t11:
        Score of 0.26 vs TideEval:                      11 - 35 - 34  -> =  comp. to v0.47t3
        Score of SF14.1/0ply vs. TideEval:              79 -  0 - 1   -> =
        Score of SF14.1/4ply/1600 vs. TideEval:        301.- 56 - 43  -> -6.5
        Score of *SF11-64/0ply vs TideEval:             79 -  0 - 1   -> =
        Score of *SF11-64/4ply/1600 vs TideEval:       330 - 37 - 33  -> +6
        lichess_db_puzzle_230601_410-499-mateIn1.csv:      477 failed -> =   comp to v0.47t3
                                        AvoidMateIn1:     2015 failed -> =
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:   565 failed ->
        lichess_db_puzzle_230601_2k-9xx.csv:               723 failed ->

    2023-09-09 v47t22:
        Score of 0.26 vs TideEval:                      21 - 40 - 19  -> -12.5  comp. to v0.47t3
        Score of SF14.1/0ply vs. TideEval:              76 -  1 - 3   -> +2.5
        Score of SF14.1/4ply/1600 vs. TideEval:        286.- 71.- 42  -> -2
        Score of *SF11-64/0ply vs TideEval:             77 -  0 - 3   -> +2
        Score of *SF11-64/4ply/1600 vs TideEval:       336 - 41 - 23  -> -2
        lichess_db_puzzle_230601_410-499-mateIn1.csv:      482 failed -> -5    comp to v0.47t3
                                        AvoidMateIn1:     2015 failed ->
        lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:   565 failed -> =
        lichess_db_puzzle_230601_2k-9xx.csv:               723 failed ->

    2023-09-09 0.47t-lowtide10 (=v47t34 online):
        Score of 0.26 vs TideEval:                      14 - 41 - 25  -> +6.5  comp. to v0.47t22
        Score of SF14.1/0ply vs. TideEval:              78 -  0 - 2   -> -1.5
        Score of SF14.1/4ply/1600 vs. TideEval:        300 - 57 - 43  -> -6.5
        Score of *SF11-64/0ply vs TideEval:             80 -  0 - 0   -> -3
        Score of *SF11-64/4ply/1600 vs TideEval:       332.- 35.- 32  -> +6.5

    -------------------------------------------------------------------------------------------
    0.47u21:
        Score of 0.26 vs TideEval:                      12 - 31 - 37  -> +7  comp. to v0.47t34
        Score of SF14.1/0ply vs. TideEval:              79 -  0 - 1   -> -1
        Score of SF14.1/4ply/1600 vs. TideEval:        291 - 64 - 45  -> +5.5
        Score of *SF11-64/0ply vs TideEval:             78 -  0 - 2   -> +2
        Score of *SF11-64/4ply/1600 vs TideEval:       332.- 39 - 28.  -> -2
        lichess_db_puzzle_230601_410-499-mateIn1.csv:      459 failed -> +23   comp to v0.47t22
                                        AvoidMateIn1:     1996 failed -> +19
    checkBlocking corrections:
    sign:  lichess_db_puzzle_230601_410-499-mateIn1.csv:   439 failed -> +20   comp to v0.47u21
                                        AvoidMateIn1:     1995 failed -> +1
    behind:lichess_db_puzzle_230601_410-499-mateIn1.csv:   416 failed -> +43   comp to v0.47u21
                                        AvoidMateIn1:     1960 failed -> +36
    u23:   lichess_db_puzzle_230601_410-499-mateIn1.csv:   434 failed -> +25   comp to v0.47u21
                                        AvoidMateIn1:     1960 failed -> +36
    u25:   lichess_db_puzzle_230601_410-499-mateIn1.csv:   434 failed -> +25   comp to v0.47u21
                                        AvoidMateIn1:     1929 failed -> +67
    0.47u26:
        Score of 0.26 vs TideEval:                      10 - 39 - 31  -> -2  comp. to v0.47u21
        Score of SF14.1/0ply vs. TideEval:              79 -  0 - 1   -> =
        Score of SF14.1/4ply/1600 vs. TideEval:        310 - 51 - 39  -> -12
        Score of *SF11-64/0ply vs TideEval:             79 -  0 - 1   -> -1
        Score of *SF11-64/4ply/1600 vs TideEval:       342 - 33 - 25  -> -6.5
       lichess_db_puzzle_230601_410-499-mateIn1.csv:   434 failed -> +25   comp to v0.47u21
                                        AvoidMateIn1: 1929 failed -> +67
    0.47u29:
        Score of 0.26 vs TideEval:                      16 - 33 - 31  -> -5  comp. to v0.47u21
        Score of SF14.1/0ply vs. TideEval:              80 -  0 - 0   -> -1
        Score of SF14.1/4ply/1600 vs. TideEval:        307 - 54 - 39  -> -1.5
        Score of *SF11-64/0ply vs TideEval:             78 -  0 - 2   -> =
        Score of *SF11-64/4ply/1600 vs TideEval:       335 - 34 - 31  -> =
       lichess_db_puzzle_230601_410-499-mateIn1.csv:   411 failed -> +48   comp to v0.47u21

     24.9.2023:                                   AvoidMateIn1: 1920 failed -> +76
    47u33: = u32 but without ChessBoard:624 benefit>>1 if same piece type approaches => worse
    47u34: = u32 but less reduction at Square:1135 more like before changes: >>1 instead of >>2, + no more else, but always benefit>>=1  => worse
    47u35: = u32 but change at Square:1135 (again >>2, but) no more else, but always benefit>>=1
    47u36:ok about u31 (>>2, else) -> vs14++ 309.5/35 vs11= 346/21
    47u37: = u36 - but without prevAddAttacker.color()!= Square:1192   vs14-: 317./35--18(to u21)  vs11-: 347./22--
    47u38: = u36 - but without reduction for same PieceType in Square:1323  vs14=: 309/36-13.5 vs11+: 339./31-2
    47u39: = u36 - but without correction in line Square:1819 493 1968   vs14: 316/35 vs11: 335/32
    47u40:ok = u39 - but with correction in line Square:1819             vs14: 315/40 vs11: 346/26.
    47u41:ok = u40 - but with change for clearCheckGiving in Square:1986  13/25 vs14:, 76/4     305/41        vs11: 78/2 340/27
    47u42: = u41 - VPOS:1043 replaced by old (wrong?) code: -
    47u43: = u41 - VPOS:1381 took out exception for hasPiece of opponent color vs14: 313/34.  vs11: 344/27.
    47u44: = u41 + new treatment of already blockers in VPOS:1433+1378   vs14: 313/37(-6)  vs11: 338/29+4(vs.u41)
    47u45: = u44 +attempt to improve --
    47u46: = u44 +attempt to improve -
    47u47 ?? 46?? : = u44 +attempt to improve -              vs14:312/38 vs11: 345./27
    47u50: still VPOS->benefitToBlockers:                        (vs.u41)    12/35(+11)vs14: 75/4(+.), 310./37(-4.)  vs11: 77/3(+1), 338/25.(+.)
    47u50: commited VPOS->benefitToBlockers:                     (vs.u21)    12/35(-1) vs14: 75/4(+3.),310./37(-13.) vs11: 77/3(+1), 338/25.(-4.)
    47u52 - for comparision without all VPOS->benefitToBlockers changes (=u41?): 15/29 vs14: 75/4      320/34        vs11: 78/2      342/28
    47u53 - for comparision without all VPOS->benefitToBlockers changes (=u41?): 13/25 vs14: 75/5      320/34        vs11: 78/2      342/28
    47u50: still VPOS->benefitToBlockers:                        +-ggü. u53    12/35+6 vs14: 75/4(-.), 310./37(+6)   vs11: 77/3(+1), 338/25.(+1.) (vs.u53)
    47u50: commited VPOS->benefitToBlockers:          (vs.u21)    12/35(-1) vs14: 75/4(+3.),310./37(-13.) vs11: 77/3(+1), 338/25.(-4.)
    nun etwas besser als u41, aber immer noch schlechter als u21...
    47u21: - for comparison, orig nr. see above.      ----------> 12/37     vs14: 79/1      291/45        vs11: 78/2      332./28.
    47u54=50: commited VPOS->benefitToBlockers:       (vs.u21)    12/35(-1) vs14: 75/4(+3.) 315/35(-17)   vs11: 77/3(+1)  347./25(-9)
    u56++                                                        16/24(-8.) vs14: 76/3(+2.) 299./46(-4)   vs11: 78/2(=)   330/31(+2.)
    u57-
    u58     corrected Square:2153 luftBenefit                    16/24(-7.) vs14: 76/3(+3)  310./42(-12)  vs11: 77/3(+1)  330./31(+1)
    u61     reduced luftBenefit                       (vs.u21)   16/24(-7.) vs14: 76/3(+3)  302./45(-6)   vs11: 78/2(=)   336/32(=)
    u63: only little luft benefit + corr of Square:2111 not pieceType  13/27(-4.) vs14: 76/3(+3) 316/35.(--)    vs11: 78/2(=) 337/30(-3)
    u66: restored old giving Luft methods and commented out new attempt.
                                                                 19/30(+1) vs14: 76/3(+3) 307/41(-10)    vs11: 79/1(-1) 337/40(-3)
    u70: even a little closer to u21, but corrected benefit for checking when coverage proportion is used for black king
                                                      (vs.u21)   19/28(-8) vs14: 79/1(=)  308/40(-11)   vs11: 77/3(+1)   331./31(+2)
            lichess_db_puzzle_230601_410-499-mateIn1.csv:      483 failed -> -24   comp to v0.47u21
                                            AvoidMateIn1:     1971 failed -> +25
            lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:   459 failed
            lichess_db_puzzle_230601_2k-9xx.csv:               728 failed
    u70a: like u70, but only correction for pawns moving straight - no futureLevel correction for covering own pieces
                                                      (vs.u21)   12/30(-8) vs14: 77/2(+1.)  305/41.(-9)   vs11: 76/4(+2)   334/29(-.)
            lichess_db_puzzle_230601_410-499-mateIn1.csv:      485 failed -> -24   comp to v0.47u21
    u70b: like u70a + correction of additionally attack benefits
                                                      (vs.u21)   16/30(-10) vs14: 78/2(+1)  298/45.(-3)   vs11: 78/2(=)   330/33.(+7.)
            lichess_db_puzzle_230601_410-499-mateIn1.csv:      485 failed -> -24   comp to v0.47u21
    u71: relEval correction for pawns moving straight + futureLevel correction for covering own pieces
                                                      (vs.u21)   11/34(+1) vs14: 76/4(+3)  295/45(-2)   vs11: 79/1(-1)   333./28(-.)
            lichess_db_puzzle_230601_410-499-mateIn1.csv:      524 failed -> -65   comp to v0.47u21     #15,20,21,25,27,31,35,44,62,89,93,105,109,115,120,129,132,133,134,136,138,152,170,173,174,185,193,201
                                            AvoidMateIn1:     1983 failed -> +13
            lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:   517 failed
            lichess_db_puzzle_230601_2k-9xx.csv:               750 failed
    u71b: 70b + futureLevel correction for covering own pieces = u71 + correction of additionally attack benefits from u70b
                                                      (vs.u21)   17/31(-10) vs14: 76/3(+2.5)  308/35(-13.)   vs11: 76/4(+2)   341./27(-5.)
            lichess_db_puzzle_230601_410-499-mateIn1.csv:      403 failed -> +56   comp to v0.47u21     #15,20,21,25,27,31,35,44,62,89,93,105,109,115,120,129,132,133,134,136,138,152,170,173,174,185,193,201
                                            AvoidMateIn1:     1978 failed -> +18
            lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:   454 failed
            lichess_db_puzzle_230601_2k-9xx.csv:               689 failed
    u72: 71b + little modification/simplification of additionally futire attack benefits
                                                      (vs.u21)   14/34(-2.) vs14: 74/4(+4)  306./41(-10)   vs11: 78/2(=)   336/30(-1)
            lichess_db_puzzle_230601_410-499-mateIn1.csv:      400 failed -> +59   comp to v0.47u21
                                            AvoidMateIn1:     1977 failed -> +19
    u73: 72 + little less  benefit reduction if preparer exists, Square:1142
                                                      (vs.u21)   15/38(-1) vs14: 75/4(+3.)  304/40(-9)   vs11: 78/2(=)   338/32(-1)
            lichess_db_puzzle_230601_410-499-mateIn1.csv:      402 failed -> +57   comp to v0.47u21
                                            AvoidMateIn1:     1981 failed -> +15
            lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:   452 failed
            lichess_db_puzzle_230601_2k-9xx.csv:               698 failed

    u75: 72 + little less less :-) benefit reduction if preparer exists, Square:1142
                                           (vs.u21)   10/35(+2) vs14: 73/4(+4.)  303/44(-6.)   vs11: 77/3(+1)   335./28(-2)
            lichess_db_puzzle_230601_410-499-mateIn1.csv:      402 failed -> +57   (76:388) (77:435) comp to v0.47u21
                                            AvoidMateIn1:     1980 failed -> +16
            lichess_db_puzzle_230601_410-499-NOTmateIn1.csv:   452 failed
            lichess_db_puzzle_230601_2k-9xx.csv:               698 failed   (76:682)
    u91: u75 + experiment with EvaluatedMove comparision (thr=20,30,45,50)
                                                      (vs.u75)   x() vs14: 75/5(-.)  311/35(-8)   vs11: 80/0(-3)   (~-15)
    u92: u75 + experiment with EvaluatedMove comparision: (thr=35,45,56,65) + negative bias (of thr>>2) from the beginning
                                                      (vs.u75)   16/22(-9.) vs14: 75/4(-1)  309/33.(-8.)   vs11: 80/0(-3)   332/31(+3)
    u93: u75 + experiment with EvaluatedMove comparision: (thr=36,45,56,65) + negative bias (of only 4) from the beginning
                                                      (vs.u75)   14/25(-7) vs14: 75/4(-1)  306./38(-5)   vs11: 79/1(-2)   331./29(+2.5)
    u94: u93, but thr=28,45,56,65  -> worse
    u96: u93, but added former code with probablyBetter = true when delta > (comparethreshold >> 1)
                                                      (vs.u75)   11/25(-5.) vs14: 80/0(-5.)  291/46(+7)   vs11: 78/2(-1)   335./33.(+3)
    u98: u93, but less strict with low negative if in the very good range the levels before (EvaluatedMove:162)  -> worse 
    u99: u93, without u98-experiment, but  thr=36,*41*,60,65
                                                      (vs.u75)   11/25(-5.) vs14: 80/0(-5.)  291/46(+7)   vs11: 78/2(-1)   335./33.(+3)
    u100: u99, but thr=36,48,56,65                    (vs.u75)           () vs14: 80/0(-5.)       /(+7)   vs11: 79/1(-2)   /(+9)
    u101: u99, but thr=40,50,60,70 -> -2 to -6 worse than u99
    u102: u99, but thr=33,48,56,65 -> -- worse than u99
    u103(102): u99, but thr=38,48,56,65 -> - worse than u99
    u104: u99, but thr=35,48,56,65 -> ... retestet u105=u100 - jitter in results of 3x u100 is +/-10 so hard to tell wha is an improvement...
    now avg of 6 runs a 400 games:
    u106: u99 but thr=36,48,56(,56):        (vs.u75)  10/25(-5) vs14: 79/1(-4.) 307/37.(-5)   vs11: 80/0(-1.)   332/32(+4)
    u107: u99 but thr=36,48,60(,60):        (vs.u75)  10/27(-4) vs14: 79/1(-4.) 300/45(+3.5)  vs11: 80/0(-1.)   331./34(+5)
    u108: u99 but thr=36,48,62(,62):      - (vs.u75)   9/28(-3) vs14: 78/2(-3.)  303/37(-3.)   vs11: 80/0(-1.)   335/30(+1)
    u109(108): u99 but thr=36,48,59(,59):   (vs.u75)  11/23(-7) vs14: 79/1(-4.) 301/44.(+1.)  vs11: 80/0(-1.)   335/33(+2.)
    u110: u99 but thr=36,48,60,63:          (vs.u75)   7/28(-2) vs14: 78/2(-3.)  308./38(-7)   vs11: 80/0(-1.)   332./27(+1)
    u111=107: u99 but thr=36,48,60,60:      (vs.u75)  10/27(-4) vs14: 78/2(-3.)  302/40.(-1)   vs11: 80/0(-1)    333./32(+3)
                                            (vs.u75)  10/27(-4) vs14: 78/2(-3.)  302/39(-2)    vs11: 80/0(-1)    334/30(+2)
    u112=107: u99 but thr=36,48,60,60:(2x400)(vs.u75) 10/27(-4) vs14: 79/1(-4.) 306/38.(-6)  vs11: 80/0(-1.)   332/33(+4.)

    u113: u112 with clashEval-correction by last taker color 
                                            (vs.u75)  11/24(-6) vs14: 79/1(-4.) 306/38.()  vs11: 80/0(-1.)   332/33()
    u114: u113 with much more clashEval-correction (+/-30, *2 for >200) 
                                            (vs.u75)  11/24() vs14: 79/1(-4.) 308./38(-)  vs11: 80/0(-1.)   339/25.(-)
    u115=u100?: thr=36,48,56,65             (vs.u75)  10/24(-5.) vs14: 78/2(-3.)  303/39(-2.)   vs11: 80/0(-1.)   333./30(+2)
    u116: u100, thr=36,48,56,65 , but old delta-dependant reduction of thr
                                            (vs.u75)    9/26(-4)  vs14: 78/2(-3.)  303/39(-2.)   vs11: 80/0(-1.)  334/31(+2)
    u117: u116, but thr=36,48,56,60         (vs.u75)   10/27(-4)  vs14: 79/1(-4.)  304/35.(-5)   vs11: 80/0(-1.)  328/32.(+6)
    u118: u116, +slightlyBetter is better   (vs.u75)   10/25(-5)  vs14: 79/1(-4.)  299/43(+1.)   vs11: 79/1(-.)   331/32.(+4.)  
    u119: u118, but thr=36,48,56,63         (vs.u75)   10/25(-5)  vs14: 79/1(-4.)  299/43(+1.)   vs11: 79/1(-.)   337/28.(-.)
    u120(119): u118, but thr=36,48,56,59    (vs.u75)   10/25(-5)  vs14: 79/1(-4.)  300./39(-1.)  vs11: 79/1(-.)   338./26.(-1.)
    u121: u118, but thr=36,48,57,66         (vs.u75)    8/27(-3)  vs14: 79/1(-4.)  296./41.(+2)  vs11: 79/1(-.)   335./27(=)
    u122: u118, but thr=36,48,57,65         (vs.u75)    8/27(-3)  vs14: 79/1(-4.)  301./41 (-.)  vs11: 79/1(-.)   337/32(+1.)
    u123: u118, but thr=36,48,56,65         (vs.u75)   10/25(-5)  vs14: 79/1(-4.)  297/42(+2)    vs11: 79/1(-.)   335/32.(+2.) *
    
    47v1: u123, + sqs with K also processed in calcFutureClashEval()
                                         (vs47.u123)   13/22(-3)  vs14: 76/3(+2.)  294/48(+4.)   vs11: 79/1(=)    335./30(-1.)
      v2: only preceed opponent K        (vs47.u123)   13/32(+2)  vs14: 77/1(+1)   300/42(-1.)   vs11: 80/0(-.)   337./26(-4.)
      v3: = v1 with killable-check for fork-detection 
                                         (vs47.u123)   13/22(-3)  vs14: 76/3(+2.)  299./40.(-2)  vs11: 78/2(+1)   330./32(+2)   
      v4: = v3, but varied killable-check(vs47.u123)   13/23(-2.) vs14: 76/3(+2.)  297/45.(+2)   vs11: 78/2(+1)   335./30.(-1)  ***

      v5: = v4, clashContribCorrection     (vs47.v4)   19/24(-2.)                         vs11: 79/1(-1)   335./26.(-2)  on Freitag
      v5: = v4, clashContribCorrection     (vs47.v4)     /()  s14:   /()     /()     vs11: 79/1(-1)   335/29 (-.)  on Oz/BodhiVM
      v6: = v5, clashContrib>>1            (vs47.v4)   18/22(-3)                          vs11: 79/1(-1)   340/30.(-2)  on Freitag
      v6: = v5, clashContrib>>1            (vs47.v4)     /()  s14:   /()     /()     vs11: 79/1(-1)   335./34(+2)  on Oz/BodhiVM
      v7: = v5, clashContrib*0.88          (vs47.v4)   19/22(-3.)                         vs11: 78/2(=)   331/31.(+3)   on Freitag  ++
      v7: = v5, clashContrib*0.88          (vs47.v4)     /()  s14: 78/2(-1.) 300/44.(-2)  vs11: 78/2(=)   340/29.(-3)   on Oz/BodhiVM
      v8: = v7 + pawnPromo fl-1 correction (vs47.v4)   19/19(-5)                          vs11: 78/2(=)   333/25 (-1.)  on Freitag 
      v8: = v7 + pawnPromo fl-1 correction (vs47.v4)              vs14: 80/0(-3.)  271./31.(+5.)  vs11:  78/2(=) 334./27.(-1)  on Oz/BodhiVM

      v9: = v8 + PromoDefendFL-1 test      (vs47.v4)   22/18(-7)  vs14: 76/()  297/()     vs11: 77/3(+1)  337/24 (-4)   on Freitag 
      v10: =v8 + PromoDefendFL+1 test      (vs47.v4)   ERRca(-5)  vs14: 76/()  297/()     vs11: 77/3(+1)  331./25. (-4)   on Freitag 
      v11: =v8(pawnPromo fl-1 corr.) but clashContrib*0.94 
                                           (vs47.v4)   ()  vs14: 80/0(-3.)  /()     vs11: 77/3()  331./25. ()   on Oz/BodhiVM 
 
    48a - starting point of LowTide2 refactoring (first run after refactoring Evaluation, Hashes of Evaluations and EvaluatedMoves, with only basic error eliminition, repairing of test cases still open :-))
                                                       XX  vs14: XX  xx)    vs11: 80/0   352/23
            lichess_db_puzzle_230601_410-499-mateIn1.csv, AvoidMateIn1, NOTmateIn1.csv:   431, 1928, 439 failed
            lichess_db_puzzle_230601_2k-9xx.csv:                                          681 failed
    48b - "fixed" LowTide2-aggregation from vPces to predecessors with not directly -1 distance  + fixed seemingly old bug in lost contributions if target square has a piece with contribution 
                                                       XX  vs14: XX  xx)    vs11: 79/1   351/20
    48c - no pass down from vPces with NoGo in aggr.   14/27  vs14: XX  xx)         vs11-Freitag: 78/2 351/20
            lichess_db_puzzle_230601_410-499-mateIn1.csv, AvoidMateIn1, NOTmateIn1.csv:   427, 1903, 419 failed *
            lichess_db_puzzle_230601_2k-9xx.csv:                                          656 failed
    48c2 =48c with omaxbenefits/3 (instead of /4)     16/21  vs14: XX  xx)         vs11-Freitag: 78/2 346./21
    48c3 =48c with omaxbenefits/2                     15/21  vs14: 78/2 318/30     vs11-Freitag: 78/2 351/20
    48c4 =48c2 with no pass down to non-shorter vPces 17/22  vs14: 78/2 318/33     vs11-Freitag: 77/3 348./20.
    48c5 =48c4 + pass down to much shorter vPces      14/22  vs14: 76/3 314./28.   vs11-Freitag: 79/1 347/21.
    48c6 =48c5 + time warp much much shorter vPces    14/22  vs14: XX  xx          vs11-Freitag: 78/2 347/22  
    48c7 =48c6 + bug fix corrupting legal move list   16/24  vs14: 76/3 311./32.   vs11-Freitag: 78/1 341/23. +
    48c8 =48c7 + d==0 aggreg correction               16/19  vs14:                 vs11-Freitag: 78/1 342/24
    48d king fork detection modification              17/19  vs14: 78/1 313/32     vs11-Freitag: 78/1 345./23.
                                                             vs14: 78/1 314./34.   vs11-OzBodhiVM: 78/1 347./21                                                                         
    48d1 king fork: reincrease avoid walking into fork 14/20 vs14: 76/2 319./30.   vs11-OzBodhiVM: 78/2 348./21.
    48e changed control of squares and fl-1           16/18  vs14: 79/1 322./30.   vs11-OzBodhiVM: 78/2 353./20.
    48e1 only changed control of squares (fl unchged) 17/17  vs14: 78/2 315./31    vs11-OzBodhiVM: 79/1 343/22. 
    48e2 more defend around king                      22/23  vs14: 75/2 307./31.   vs11-OzBodhiVM: 78/2 346./23.
            lichess_db_puzzle_230601_410-499-mateIn1.csv, AvoidMateIn1, NOTmateIn1.csv:   446, 1895, 444 failed
            lichess_db_puzzle_230601_2k-9xx.csv:                                          687 failed
    48e3: e2+benefit around opp king if attackCountDelta>=0  19/19  vs14: 78/2 310./31. vs11-OzBodhiVM: 78/2 347./24.
    48e4: e2+benefit around opp king if attackCountDelta<=0  14/21  vs14: 76/3 316/31.  vs11-OzBodhiVM: 78/2 346./25.
    48e5: e4+higher benefit around king defense              16/26  vs14: 75/4 314/29   vs11-OzBodhiVM: 78/2 346/22.  -
    48e6: e5+higher check blocking if 0 kingmoves            16/26  vs14: 75/4 317./28. vs11-OzBodhiVM: 78/2 350./21.
    48e7: e5+old check blocking if 0 kingmoves + also a little for nogos and conditional
                                                             17/25  vs14: 75/4 316./33. vs11-OzBodhiVM: 78/2 351/19.
    48e8: reduced king area defending a bit                  13/25  vs14: 76/3 316/32   vs11-OzBodhiVM: 78/2 348./21.
    48e9: fixed little for nogos and conditional             13/24  vs14: 77/2 317./31  vs11-OzBodhiVM: 78/2 347/21. 
            lichess_db_puzzle_230601_410-499-mateIn1.csv, AvoidMateIn1, NOTmateIn1.csv:   464, 1902, 486 failed
            lichess_db_puzzle_230601_2k-9xx.csv:                                          693 failed
    48e10: =e4 just little diff. king defend if delta<=-1    13/25  vs14: 76/3 312./33. vs11-OzBodhiVM: 78/2 346/21 +

    48f:  =e10, but aggr. only to predecessors without nogo    x    vs14: 75/3 302/35.  vs11-OzBodhiVM: 78/1 334./24 +
        (cmp to 47v4 = before aggregation=LowTide2)        (x/ca+1) vs14: (+.) (-7.)    vs11-OzBodhiVM: (-.) (-3) 
    48f2: f even without aggregation to conditional pred.    13/26  vs14: 74/6 307./30. vs11-OzBodhiVM: 80/0 335/26 -
    48f3=f: lichess_db_puzzle_230601_410-499-mateIn1.csv, AvoidMateIn1, NOTmateIn1.csv:   435, 1901, 449 failed
            lichess_db_puzzle_230601_2k-9xx.csv;  ChessBoardTest:                         684 failed;   29 of 140 failed
    48f4: f3 even without aggregation down from NoGo vPces    16/21  vs14: 75/4 296/38  vs11-OzBodhiVM: 78/1 329/27 ++
                                                                        r:70m17s,u:144m56s           r:74m26s,u:160m22s
backwards test of earlier versions for comparison: + real + user time for runLong (4*2*400 test games)
    44n:   17/33 (r:10m21,u:14m10)    vs14: 74/4 304/48  (r:68m11,u:138m36)      vs11-OzBodhiVM: 77/3 344./29  (r:74m41,u:159m27)
    47u21: 11/39 (r:11m27,u:15m36)    vs14: 79/1 307./41 (r:70m38,u:143m26)      vs11-OzBodhiVM: 78/2 337./28. (r:80m7,u:169m26)
    47v4(vs47.u123)   13/23(-2.)      vs14: 76/3(+2.)  297/45.(+2)               vs11-OzBodhiVM: 78/2(+1)   335./30.(-1) 

    48g: 48f4 + changed checking/defending benefits and mate detection:
           15/20 (r:9m44,u:13m39)     vs14: 76/3 292/40 (r:70m38,u:145m8)        vs11-OzBodhiVM: 78/1 325/32 (r:79m32,u:169m55)
            lichess_db_puzzle_230601_410-499-mateIn1.csv, AvoidMateIn1, NOTmateIn1.csv:   458, 1854, 464 failed          
            lichess_db_puzzle_230601_2k-9xx.csv;  ChessBoardTest:                         735 failed;   29 of 140 failed  
           (vs47v4) (-2.)                   (=)  (=)                                           (-.)  (+6)
    48g2: g+Contrib for covering NoGo checkmate  (vs48g)    
           14/20(+.) (r:9m7,u:13m2)   vs14: 76/3 295./34(-5) (r:70m55,u:146m15)  vs11-OzBodhiVM: 78/1 324./33.(+1) (r:x,u:x) -
            lichess_db_puzzle_230601_410-499-mateIn1.csv, AvoidMateIn1, NOTmateIn1.csv:   458, 1854, 465(-1) failed          
    48g3: g2+ continue(stops) for Nogos after (rare) Contrib for covering for checkmate  (vs48g)
    (48g4? with wrong name g3?       ? vs14: 76/3 296./37.(r:71m24,u:145m45) ) --
    48g5=3: 14/20 (r:9m27,u:13m15)    vs14: 76/3 286/42. (r:71m44,u:146m30)      vs11-OzBodhiVM: 78/1 323/30 (r:x,u:x) ++
    (48g6 = g5 without mobility benefit:  12/26  vs14: 76/3 306/31. vs11: 79/1 338./23 ) --
    48g7: g5 + checking-fork reduced if king can cover (vs48v5)
           14/18 (r:10m7,u:13m29)     vs14: 76/3  292./39. (r:71m29,u:145m48)    vs11-OzBodhiVM: 79/0 331/30 (r:81m8,u:171m18)
    48g7x=g5: vs11: 78/1 328/29                                                    
    48g7x=g5?=g3?: 14/20(r:9m30,u:13m23)  vs14: 76/3    291./41(r:79m52,u:160m51) vs11-OzBodhiVM: 78/1     325/28 (r:81m8,u:171m18)
                                          vs14: 76/3    293./40                                   78/1     331/28.
                                          vs14: 76/3    296./36                                   78/1     328/28 (2x800 samples here, not 4x)
                                    avg:                294/39  (max delta with same code: +/-5!)          328/28 (+/-3)
    + checking-fork reduced by X* forkingPiece if king can cover (all vs g7x=g5-avg)
    48g12=g5, but *0.5       14/18(-1)    vs14: 76/3(=) 291/42 (+3)               vs11-OzBodhiVM: 79/0(-1) 328/29 (+.)
    48g11-or-not?(2) rerun   14/18        vs14: 76/3    291./40.                  vs11-OzBodhiVM: 79/0     327/30.
    48g11=g5, but *0.62      15/17(-2)    vs14: 76/3(=) 289/41.(+4)               vs11-OzBodhiVM: 79/0(-1) 325./29.(+2) +++
    48g8 =g5, but *0.75      14/18(-1)    vs14: 76/3(=) 291/38 (+1)               vs11-OzBodhiVM: 79/0(-1) 325/32. (+4)(3x800 samples) **
    48g10=g5, but *0.87      15/17(-2)                                            vs11-OzBodhiVM: 79/0(-1) 326./31 (+2.)  -
    48g9 =g5, but *0.93      14/18(-1)    vs14: 76/3(=) 292./40(+.)               vs11-OzBodhiVM: 79/0(-1) 327/29  (+1) -
    
    48h  =g10 + Dist.corr. for sliding pieces  14/21    vs14: 76/3 294./39.   vs11-OzBodhiVM: -  
    48h2 =g11 + -"- (vs.g12) 13/24(+4.)   vs14: 76/3(=) 293/39.(-3)               vs11-OzBodhiVM: 78/1(+1) 327/30(-1) 
            lichess_db_puzzle_230601_410-499-mateIn1.csv, AvoidMateIn1, NOTmateIn1.csv:   457, 1857, 467 failed          
            lichess_db_puzzle_230601_2k-9xx.csv;  ChessBoardTest:                         735 failed;   30 of 140 failed  
    48h3 +CBoard:1279Id2TypeCorr  15/15   vs14: 75/4 291/27               vs11-OzBodhiVM: 78/2 330/22(-1) 
    48h4 = 48h3 +CBoard:1279Id2TypeCorr+noPawnFl-1
                             12/24        vs14: 76/4 286/39 (1run)                vs11-OzBodhiVM: 78/1 334./29. (1run)
                                                76/4 291/38. (4runs)                
    48h5 -> see testrow-graph
    48h6                     14/24        vs14: 73/4 288/42.                      vs11-OzBodhiVM: 78/1 329/30. +++ *** (for 1run: r22m6s u47m31s)
    48h7 stop at 5 bestMoves 15/19        vs14: 76/1 291/38.                      vs11-OzBodhiVM: 79/1 325./30. (for 1run: r23m4s u48m59s->not faster)


    49a1 preparation mv.sel. 14/12(!?!)   vs14: 74/5 291./31(r70u144)             vs11-OzBodhiVM: 79/1 327/25 
    49a2undone3              14/18        vs14: 76/3 291/40                       vs11-OzBodhiVM: 79/1 327./25. 
    49a2undone3doneback1     14/18        vs14: 76/3 293/40                       vs11-OzBodhiVM: 79/1 330/30. 
    49a2undone3doneback1     14/18        vs14: 76/3 x/40                         vs11-OzBodhiVM: 79/1 330/30. 
    49a2undone3doneback2     14/18        vs14: 76/3 292./39 (1run)               vs11-OzBodhiVM: 79/0 335/28 (1run) 2nd: 331/28. (1run=2x400)
    49a2undone3doneback3+CBoard:1316Id2TypeCorr
                             13/18        vs14: 76/4 291./45 (1run)               vs11-OzBodhiVM: 79/0 322/30 (1run) 
    49a3 (back at a1 again + corrections of48h-h4)
                             14/17        vs14: 76/4 286/39               vs11-OzBodhiVM: 79/0 327./29. (4runs)  ++
            lichess_db_puzzle_230601_410-499-mateIn1.csv, AvoidMateIn1, NOTmateIn1.csv:   459, 1854, 467 failed          
            lichess_db_puzzle_230601_2k-9xx.csv;  ChessBoardTest:                         738 failed;   30 of 140 failed  
    
    !! 49a5 experiment without oppMoveConsideration: 27/22  vs14: 79/1 373./13       vs11-OzBodhiVM: 78/2 381/7. ---!!

    49a7-backtonormal/notYetFully?+opp15+pawn110: 18/17  vs14: 75/5 301/30(6x400) vs11-OzBodhiVM: 79/1 331./26(6x400)
    49a8=a3?                 14/18        vs14: 76/3 305./31                      vs11-OzBodhiVM: 79/0 324./31.

    49b first recursion 4ply 17/22(r10m59,u15m23=+16%)  vs14:  77/2 330/32.(r84m18,u171m18=+18%) vs11-OzBodhiVM: 80/0 356./20.(r107m24,u224m50=+28%)
            lichess_db_puzzle_230601_410-499-mateIn1.csv, AvoidMateIn1, NOTmateIn1.csv:   555, x1829, 556 failed          
            lichess_db_puzzle_230601_2k-9xx.csv;  ChessBoardTest:                         808 failed;   32 of 140 failed  
    49b-2p 2ply  as always   14/18(r9m6,u13m15)  vs14:  76/3 293/40. r(71m11,u145m15) vs11-OzBodhiVM:  79/0 330./28. (r83m12,u175m33)

    49d      (deltaValue<=0)               14/17                                  vs11-OzBVM: 79/0 325/30
    49e-2p:  (deltaValue<DELTAICAREABOUT)  15/20                                  vs11-OzBVM: 80/0 336/27.
    49e-4p:  -"-                           18/27                                  vs11-OzBVM:      368./15.
    49f_2:   (no deltaValue, countAttack)  12/19                                  vs11-OzBVM: 79/1 335.2/28.5
    49f_2+2: -"-                                                                  vs11-OzBVM: 79/1 366/17.
    49g_2:   (deltaValue<=0, countAttack)  15/20                                  vs11-OzBVM: 80/0 332./29
    49g_2+2: -"-                                                                  vs11-OzBVM: 80/0 363/18.  (best of 4p so far, but still much worse then 2p)
    49h_2p:  dV<=0,cA, check not hindering k moves 15/20                          vs11-OzBVM: 80/0 331./27
    49h_2+2: -"-                                                                  vs11-OzBVM: 80/0 381/16

    49i_2p:  fixed if in moveSeqIsHinderingMove  15/20  vs14: 76/2 306./33.       vs11-OzBVM: 80/0 331/28
            lichess_db_puzzle_230601_410-499-mateIn1.csv, AvoidMateIn1, NOTmateIn1.csv:   458, 1847, 470 failed          
            lichess_db_puzzle_230601_2k-9xx.csv;  ChessBoardTest:                         741 failed;   30 of 140 failed  
    49i_2+1:    7/29(r10m20s,u14m9s)     vs14:  79/1 328./32.(r73m23,u148m37)     vs11-OzBodhiVM: 80/0 356./25(r84m25,u177m39)
    49i_2+2:   18/26                     vs14: 80/0 367./17                       vs11-OzBVM: 80/0 367./17

    49i2_2p: higher clashContrib for 1 piece guarding check forking square  15/19  vs14: 76/2 302/34  vs11-OzBVM: 80/0 333./28


    (48h6                    14/24        vs14: 73/4 288/42.                      vs11-OzBodhiVM: 78/1 329/30. +++ *** (for 1run: r22m6s u47m31s))
    48h7                     13/25        vs14: 73/4 292/40. (16x400)             vs11-OzBodhiVM: 78/1 325./31 (16x400)
    48h8                     13/25        vs14: 73/4 296/36.                      vs11-OzBodhiVM: 78/1 327./30.
    48h8'                    13/25        vs14: 73/4 291/41.                      vs11-OzBodhiVM: 78/1 326./30.
    48h9 reworked checkforks 14/22        vs14: 74/3 292/38  (16x)                vs11-OzBodhiVM: 78/1 327/29 (16x)
    48h10 added AbzugCapture 14/22        vs14: 74/3 293/41                       vs11-OzBodhiVM: 78/1 333/27.
    48h11 setChecking for all neighbours  11/30  vs14: 73/6 296./41.              vs11-OzBodhiVM: 79/1 331/30
    48h12 Abzugschach in checkForks       11/30  vs14: 73/6 297./39.              vs11-OzBodhiVM: 79/1 332/30.
    48h12b ?                 11/29        vs14: 73/6 304/37                       vs11-OzBodhiVM: 79/1 330/29
    48h13 move king out of forks 13/29    vs14: 74/4 301/40                       vs11-OzBodhiVM: 79/1 335/28
            lichess_db_puzzle_230601_410-499-mateIn1.csv, AvoidMateIn1, NOTmateIn1.csv:   774, 1958, 1247 failed  --  (without h11 NOTmateIn1 restores back to 475)          
            lichess_db_puzzle_230601_2k-9xx.csv;  ChessBoardTest:                         961 failed;   29 of 140 failed --  
    48h14 predecessors (not Nbs) 14/20    vs14: 77/1 318/33 (4x)                  vs11-OzBodhiVM: 78/2  342./24 (4x) --- 
    48h15?16 getDirectAttackVPcs (not Pred) 12/25    vs14: 77/2 301./36.             vs11-OzBodhiVM: 77/3 334/27
            lichess_db_puzzle_230601_410-499-mateIn1.csv, AvoidMateIn1, NOTmateIn1.csv:   457, 1859, 482 failed          
            lichess_db_puzzle_230601_2k-9xx.csv;  ChessBoardTest:                         761 failed;   29 of 143 failed  
    48h16 getDirectAttackVPcs (not Pred)    12/25    vs14: 77/2 293./36.          vs11-OzBodhiVM: 77/3 333/25


    48h6                                    14/24     vs14: 73/4 288/42.       vs11-OzBodhiVM: 78/1 329/30. +++ ** (for 1run: r22m6s u47m31s)
    vgl: 0.48h6'':                          14/24     vs14: 73/4 294/37.       vs11-OzBodhiVM: 78/1 328/28.
                                                       avg:            291/40                             328./29

    48h6+':                                 17/23     vs14: 72/5 292./38.(14x) vs11-OzBodhiVM: 78/1 328/27    =   failed mateIn1: 436
    48h6+h17+h18(0b65fda9)':                17/19     vs14: 71/7 296./41.      vs11-OzBodhiVM: 79/1 331/31.  -    failed mateIn1: 422
    48h6+h15-18(f8446713):                  16/20     vs14:                    vs11-OzBodhiVM: 79/1 330/30    =/+ failed mateIn1: 426
    48h6+h15-18+h10parts(5a02815a):         16/21     vs14: 74/3 300/41.       vs11-OzBodhiVM: 79/1 337/30.  --   failed mateIn1: 426  -- unclear why worse, there seems no "functual" chance in this commit at all...
    48h6+h15-18+h10parts(5a02815a w/o 9c653d48)':17/21 vs14: 73/4 293/43       vs11-OzBodhiVM: 79/1 329/32    ++  failed mateIn1: 431
    48h6+h15-18+h10parts(5a02815a w/changed 9c653d48:3||6)':
                                            16/21     vs14: 74/3 294/45        vs11-OzBodhiVM: 79/1 330./33 ++  failed mateIn1: 426
    48h6+'+h15-18+h10parts(5a02815a)+(9c653d48-chgd:3||6)+neg:
                                            16/21     vs14: 74/3 296./43.      vs11-OzBodhiVM: 79/1 329/32.    =
    48h23+PartOfh28(dbbef3d5+'+neg:         11/25     vs14: 76/1 301/40.       vs11-OzBodhiVM: 78/2 335./28.   -


    48h17 fixes of checkBlocking            12/25    vs14: 77/1 298./42           vs11-OzBodhiVM: 78/2 338/29    --  X
            lichess_db_puzzle_230601_410-499-mateIn1.csv, AvoidMateIn1, NOTmateIn1.csv:   414, 1792, 408 failed  ++     
            lichess_db_puzzle_230601_2k-9xx.csv;  ChessBoardTest:                         745 failed;   27 of 143 failed
    48h18 future vPce set checking          11/25    vs14: 75/2 300/39.(r72m31,u148m25) vs11-OzBodhiVM: 78/2 335/28.(r77m14,u164m35) +
    48h18-h10 Test without AbzugCapture     11/24    vs14: 75/2 298./43           vs11-OzBodhiVM: 78/2 334./28.  ? =
    48h19-h10 king covers fork correction   11/25    vs14: 75/2 295/44.           vs11-OzBodhiVM: 78/2 334./29   +
    48h20-h10 Rmd in calcKingAttacksBenefit + no more clashContrib if only  last to protect forking square
                                            11/25    vs14: 75/2 305/37            vs11-OzBodhiVM: 78/2 334./30.  -
    48h21-h10 fix fork takeback benefit calc 11/22   vs14: 75/2 301/38            vs11-OzBodhiVM: 78/2 334/30 (Square:1017) +
    48h22 incl. reduced h10, no h20, fork-cover 11/25 vs14:75/2 297./41.          vs11-OzBodhiVM: 78/2 333./29.
    48h23 restored clashContrib for last to protect forking square 
                                            10/23    vs14: 75/2 303/42. (14x)     vs11-OzBodhiVM: 78/2 334./28. -?
    48h23-no cC4l2pfs with std. contrib     11/25    vs14: 75/2 307./39           vs11-OzBodhiVM: 78/2 336/28.  -? auch?
    (48h23s with cC4l2pfs sign inverted     10/22    vs14: 75/2 304./39           vs11-OzBodhiVM: 78/2 347/29.  -  , mateIn1: 418 failed)
    48h24 calc Abzugschachs earlier and use in mateIn1Detection 
                                            10/25    vs14: 74/3 296/42            vs11-OzBodhiVM: 78/2 332./32  ++
    48h25 mateIn1 cases w/ calcDAttackVPce  13/24    vs14: 75/0 303./37.          vs11-OzBodhiVM: 78/2 337./27   --
            lichess_db_puzzle_230601_410-499-mateIn1.csv, AvoidMateIn1, NOTmateIn1.csv:   373, 1812, 438 failed  ++      
            lichess_db_puzzle_230601_2k-9xx.csv;  ChessBoardTest:                         751 failed;   26 of 143 failed  
    48h26 more mateIn1 cases                10/24    vs14: 75/0 309./43.          vs11-OzBodhiVM: 79/1 333/30 +/-
            lichess_db_puzzle_230601_410-499-mateIn1.csv, AvoidMateIn1, NOTmateIn1.csv:   202, 1659, 551 failed  ++--      
            lichess_db_puzzle_230601_2k-9xx.csv;  ChessBoardTest:                        704 failed;   24 of 145 failed  
    48h27r1 reverted ~h25 change in moveOutOfTrouble  13/20  vs14: 75/0 302./41.  vs11-OzBodhiVM: 79/1 340./28.  -

    48h27m2 reactivated+incr. change in moveOutOfTr.  17/25  vs14: 76/0 297/43    vs11-OzBodhiVM: 79/1 333./30. +
    48h28 0-0fix,extraCoverageOfKingPinnedPiece       17/25  vs14: 75/0 303./44.(nur2x) vs11-OzBodhiVM: 79/1 342/25. (4x) --??
            lichess_db_puzzle_230601_410-499-mateIn1.csv, AvoidMateIn1, NOTmateIn1.csv:  172, 1662, 549 failed  +      
            lichess_db_puzzle_230601_2k-9xx.csv;  ChessBoardTest:                        715 failed;   24 of 147 failed  
    48h29b compl.usage of extraCoverageOfKingPinnedPce 20/22 vs14: 76/0 309/37    vs11-OzBodhiVM: 79/1 339/27.
    48h29c corr. usage of extraCoverageOfKingPinnedPce 12/29 vs14: 76/0 303/41.   vs11-OzBodhiVM: 79/1 336./29
            lichess_db_puzzle_230601_410-499-mateIn1.csv, AvoidMateIn1, NOTmateIn1.csv:  144, 1659, 528 failed  +=      
            lichess_db_puzzle_230601_2k-9xx.csv;  ChessBoardTest:                        702 failed;   24 of 147 failed  
    48h30 contrib around kings
    48h31 mateIn1 when not blockable by king-pinned pce 17/22 vs14: 78/2 305./36  vs11-OzBodhiVM: 80/0 340/25 
            lichess_db_puzzle_230601_410-499-mateIn1.csv, AvoidMateIn1, NOTmateIn1.csv:  136, 1652, 536 failed       
            lichess_db_puzzle_230601_2k-9xx.csv;  ChessBoardTest:                        698 failed;   26 of 149 failed  
    48h31a contrib around kings             8/18     vs14: 75/1 305./37.   vs11-OzBodhiVM: 78/2 339./25.

    48h33 indirect mateIn1                            12/18  vs14: 76/4 334/30    vs11-OzBodhiVM: 79/1 357/21     ---
            lichess_db_puzzle_230601_410-499-mateIn1.csv, AvoidMateIn1, NOTmateIn1.csv:  135, 1655, 590 failed  --   
            lichess_db_puzzle_230601_2k-9xx.csv;  ChessBoardTest:                        702 failed;   26 of 150 failed  
    48h34 fixes for h33                               18/23  vs14: 77/2 319./33   vs11-OzBodhiVM: 79/1 346.6/23 still --
    48h35 indirect mateIn1                                   vs14: 77/3 312./35(2x) vs11-OzBodhiVM: 78/2 340/28(2x) still -
    48h36 indirect mateIn1                            18/23  vs14: 77/3 311./34     vs11-OzBodhiVM: 78/2 347./23
            lichess_db_puzzle_230601_410-499-mateIn1.csv, AvoidMateIn1, NOTmateIn1.csv:  119, 1654, 573 failed  --   
            lichess_db_puzzle_230601_2k-9xx.csv;  ChessBoardTest:                        689 failed;   24 of 149 failed  
    48h37 indirect mateIn1                            17/25  vs14: 76/3 317./33     vs11-OzBodhiVM: 78/2 344./24.
    
    48h38 fixups from (0.48h23+PartOfh28(dbbef3d5)+ little fixes +inverted forkCoverBenefit)
                                                      17/25  vs14: 76/3 311/34.   vs11-OzBodhiVM: 78/2 349/22.  +/=
            lichess_db_puzzle_230601_410-499-mateIn1.csv, AvoidMateIn1, NOTmateIn1.csv:  113, 1654, 572 failed    
            lichess_db_puzzle_230601_2k-9xx.csv;  ChessBoardTest:                        683 failed;   25 of 149 failed  
    48h39                                             17/25  vs14: 75/4 310/34    vs11-OzBodhiVM: 78/2 349./22. =  **
            lichess_db_puzzle_230601_410-499-mateIn1.csv:  110,  25 of 149 failed  
    48h40 Abzugschach in OppMoveSel. +oppMoveReeval-fix 17/24 vs14: 74/4 317./35. vs11-OzBodhiVM: 78/2 344/25  -/=
            lichess_db_puzzle_230601_410-499-mateIn1.csv, AvoidMateIn1, NOTmateIn1.csv:  110, __1654, 572 failed,  25 of 150 failed  
    48h41 add min relEval to futureClashBenefits +circ 14/23  vs14: 77/2 309./39.  vs11-OzBodhiVM: 79/1 345./26  ++
    48h42 addBetter fcB vs relEval:                   14/25   vs14: 78/1 315/35    vs11-OzBodhiVM: 79/1 346/24.  -
    48h43 2nd move tempo win for bestOppMove          22/22   vs14: 74/4 319/31.   vs11-OzBodhiVM:  80/0 346/26. -
            lichess_db_puzzle_230601_410-499-mateIn1.csv, AvoidMateIn1, NOTmateIn1.csv:  110, 1651, 623 failed,  30(!-) of 150 failed  
    48h43b allow negative oppMoveEvals                43/23 (!!--)   vs14:         vs11-OzBodhiVM: 78/2 386/7 (2x) ---
    48h44a no more 43b, fix mateIn1 blocker           25/24   vs14: 76/2 316./33.  vs11-OzBodhiVM:  80/0 342./29 
           lichess_db_puzzle_230601_410-499-mateIn1.csv, AvoidMateIn1, NOTmateIn1.csv:  110, 1639, 606 failed,    
           lichess_db_puzzle_230601_2k-9xx.csv;  ChessBoardTest:                       700 failed;   31 of 152 failed  
    48h44c 44a but no more relEval reduction for kings 22/22  vs14: 75/2 314/35    vs11-OzBodhiVM: 79/1 345/24.  +/=
    48h44d pawn improvements                          20/28   vs14: 74/5 317./44   vs11-OzBodhiVM: 79/1 347./28
    48h44d pawn improvements        also 44d? +x?     19/26                        vs11-OzBodhiVM: 79/1 352./25.  +/=
    48h44e clash calculation with pce position value  18/31   vs14: 74/5 320/44    vs11-OzBodhiVM: 79/1 349/27.
    48h44e-f e without last clasher position delta    19/26   vs14: 74/5 318/42.   vs11-OzBodhiVM: 79/1 351./26.  =/+
    48h44i' changes to blocking-fl                    17/20   vs14: 75/5 316/48.   vs11-OzBodhiVM: 78/2 345/29    +
    48h44j improve check-fork detection + reduce contrib on checking squares
                                                      17/27   vs14: 77/1 314/43.   vs11-OzBodhiVM: 78/2 340/34.   ++
    48h44k strongly reduce real forking cases         18/17   vs14: 76/4 309./39   vs11-OzBodhiVM: 80/0 336./26   ++
    48h44l slightly chaseing away possible checkers + reducing(/2) benefit for additionally attack goodMover  
                                                      15/14   vs14: 76/3 308./33   vs11-OzBodhiVM: 80/0 333./25.  +
           lichess_db_puzzle_230601_410-499-mateIn1.csv, AvoidMateIn1, NOTmateIn1.csv:  109, 1671, 598 failed,
    48h44m ineffectiveBlocker + reducing(/2.7) benefit for additionally attack goodMover
                                                      16/13   vs14: 74/6 299./30.  vs11-OzBodhiVM: 80/0 335/24.  +
    48h44n ineffectiveBlocker + (incorrect) fix "coverOrBlockBenefit = -benefit"
                                                      11/18   vs14: 74/4 299/31.   vs11-OzBodhiVM: 79/1 333/26   +/=
    48h44o extra defending fl+1 + self-blocking last king escape square (or 44p?)
                                                      10/15   vs14: 75/5 291/35    vs11-OzBodhiVM: 78/2 333./25. +
           lichess_db_puzzle_230601_410-499-mateIn1.csv, AvoidMateIn1, NOTmateIn1.csv:  115, 1604, 534 failed,
           lichess_db_puzzle_230601_2k-9xx.csv;  ChessBoardTest:                        700 failed;   38 of 156 failed  
    48h44p incEvaltoMaxOrDecreaseFor, so negative fees are not maxed away.
                                                      12/13   vs14: 75/4 304./29   vs11-OzBodhiVM: 78/2 338./23  --
           lichess_db_puzzle_230601_410-499-mateIn1.csv, AvoidMateIn1, NOTmateIn1.csv:  115, 1594, 508 failed,
    48h44q more restricted self-blocking 44p...       10/16   vs14: 75/3 301./29   vs11-OzBodhiVM: 79/1 340./22   +
    48h44p? no self-blocking 44p/q...                 12/19   vs14: 75/4 304./30   vs11-OzBodhiVM: 80/0 339/23   - 
    48h44r 44p? w/o incEvaltoMaxOrDecreaseFor         12/18   vs14: 76/3 305/31    vs11-OzBodhiVM: 80/0 335./23   +/=
    48h44s TEST reverted +/-coverbenefit + isBlack    11/18   vs14: 75/5 295./33.(6x) vs11-OzBodhiVM: 78/2 328./25(4x)  ++ :-( 
    48h44s2 TEST only reverted +/-coverbenefit        15/13   vs14: 75/3 289/37.   vs11-OzBodhiVM: 80/0 326/26.
    48h44s3 nothing reverted = 44r?                   11/17   vs14: 76/3 304/29.   vs11-OzBodhiVM: 79/1 334./23.
    48h44s4 Square:2627 wrong "-" put back in...      15/13   vs14: 75/3 294./32.  vs11-OzBodhiVM: 80/0 321/28   ++ :-(  
    48h44s5 isBlack+ contrib=defBen, but /2 for kings 14/14   vs14: 77/1 298./33(4x)vs11-OzBodhiVM: 77/2 344/19.(4x) --
    48h44s6 like s5, but only for dist2check==1       14/17   vs14: 77/3 290/36.   vs11-OzBodhiVM: 78/1 331/25.  ++ but still not like s4
    room4castling:    
    (48h44s7 s6+badly fixed make room4castling +9f5a6e88 12/15 vs14: 78/2 304/33.   vs11-OzBodhiVM: 79/1 337./22
     48h44s8 s7 with again wrong fix                   13/16   vs14: 78/2 303./32   vs11-OzBodhiVM: 79/1 338/24
     48h44s9 s7 with again wrong fix, 0 for kingContr  11/16   vs14: --             vs11-OzBodhiVM: 79/1 338/26 )
    48h44s8' s7 with corrected fix                    11/14   vs14: 78/2 300/29    vs11-OzBodhiVM: 79/1 339/22.  --
    48h44s9' s7 with corrected fix + 0 for kingContr  11/16   vs14: 77/2 305/29.   vs11-OzBodhiVM: 79/1 335./25.  =
    48h44s10 s9'+more room4castling benefit, but fl=1 13/13   vs14: 77/2 297./31   vs11-OzBodhiVM: 79/1 338./22.
    48h44s11 s10 but no motivation to clear king castling area, just not to go back there...                      --
                                                      14/14   vs14: 77/2 307./28.  vs11-OzBodhiVM: 79/1 343.1/20.5
    48h44s12: w/o fee for going back to castling area 11/14   vs14: 77/2 313./29   vs11-OzBodhiVM: 79/1 341./19.

    48h44t2: =s12 + reworked check forking            11/16   vs14: 77/2 300./32.  vs11-OzBodhiVM: 80/0 334./25
    48h44t3: =t2 + both castling area benefit+fee     12/15   vs14: 77/2 298/33.   vs11-OzBodhiVM: 80/0 336/23.
    48h44t4: =t3 + less r4c benefit                   12/17   vs14: 77/2 301/32    vs11-OzBodhiVM: 80/0 335/21.

    48h44u: bugfixed castling(!) + check counting     12/18   vs14: 75/2 285./37  vs11-OzBodhiVM:  76/2 328./26   ***
           lichess_db_puzzle_230601_410-499-mateIn1.csv, AvoidMateIn1, NOTmateIn1.csv:  114, 1600, 501 failed,
           lichess_db_puzzle_230601_2k-9xx.csv;  ChessBoardTest:                        673 failed;   36 of 157 failed  

    48h44v: move king out of pins or upcoming pins    12/18   vs14: 75/2 290/34    vs11-OzBodhiVM: 76/2 320/30  =/+
    48h44w: -bias clashes when behind on material     13/17   vs14: 75/2 288/33.   vs11-OzBodhiVM: 78/0 323./27  +-
    48h44w2: +bias clashes when ahead in material     12/24   vs14: 75/3 297./47.  vs11-OzBodhiVM: 77/3 304./33  -+
    48h44w3: less but oftener -bias when behind        9/25   vs14: 76/1 286./38   vs11-OzBodhiVM: 76/2 295/22   ++
    48h44w4: w2+w3                                    15/22   vs14: 75/2 290./47.  vs11-OzBodhiVM: 79/1 336./31.
    -> but in Test Series -> 0 -bias was seemingly best, but also vary strong variance between single tests.

    48h44x: tec.refactor not moving into pin + less for pinner
                                                      16/20   vs14: 72/4 286./38   vs11-OzBodhiVM: 77/2 320./30 +/= comp to 44v
    48h44x2: reduce pinDanger for protected pieces    16/17   vs14: 71/6 284./33.  vs11-OzBodhiVM: 77/2 323./26. +
    48h44x3: move out of existing pin                 16/17   vs14: 71/6 292./34.  vs11-OzBodhiVM: 77/2 320./26
    48h44x3: move out of existing pin                 16/17   vs14: 71/6 283/37    vs11-OzBodhiVM: 77/2 317./29
    48h44x4: lowestReasonableExtraThreatFrom          __/17   vs14:                vs11-OzBodhiVM: 77/2 321.8/26.2
           lichess_db_puzzle_230601_410-499-mateIn1.csv, AvoidMateIn1, NOTmateIn1.csv:  114, 1598, 502 failed,
           lichess_db_puzzle_230601_2k-9xx.csv;  ChessBoardTest:                        672 failed;   36 of 157 failed  
    48h44x5: fixed: x3 was never activated...+Sq:1728 23/18   vs14: 77/2 324./29   vs11-OzBodhiVM: 79/1 351./19.  ---
    48h44x6: x5 but existing pin = future pin         18/23   vs14: 73/3 286./35   vs11-OzBodhiVM: 79/0 325./25 
    48h44x7: exist.pin det. back on (>>1)             18/21   vs14: 74/3 302./29.  vs11-OzBodhiVM: 80/0 340/23. ---
    48h44x8: pinDanger/2                              15/18   vs14: 72/4 291/31.   vs11-OzBodhiVM: 77/2 326./26
    48h44x9: exist.pin det. = do nothing              __/21   vs14:                vs11-OzBodhiVM: 78/1 322/28.
    48h44x10: exist.pin det. = >>3 to go away         15/17   vs14: 72/3 285/37    vs11-OzBodhiVM: 78/1 324./26.
    48h44x11: exist.pin det.: go away only when pinner has future danger
                                                      15/20   vs14: 73/3 288./35   vs11-OzBodhiVM: 77/2 320/28
    48h44y: x11 but no more countering neg. benefits  15/20   vs14: 73/3 289/35.   vs11-OzBodhiVM: 77/2 319./28
    48h44y2:  more exist.pin                          13/21   vs14: 72/5 289/33.   vs11-OzBodhiVM: 77/2 327/24.
                                                                                                   77/2 324./25.
    48h44y3:  exist.pin det.: go away motivation only when pinner future danger > mydanger
                                                      16/21   vs14:         vs11-OzBodhiVM: 77/2 322./28.
    48h44y4: exist.pin det.>>4                        16/19   vs14: 73/4 284./34.  vs11-OzBodhiVM: 78/1 324./26
    48h44y5: consider pins in fl==2                   16/20   vs14: 73/4 287/35    vs11-OzBodhiVM: 78/1 318/28.

    48h45 two compat. parts of 49o5                +- 15/18   vs14: 72/5 286/38. u172m54  vs11-OzBVM: 79/0 325./25. u197m2
    48h46 comp full eval for oppMove, not eval0    +  12/18   vs14: 73/3 280/32. u180m56  vs11-OzBVM: 79/0 325./24  u230m24 20:71 30:49 40:33 50:18 60:12 70:7 80:5 90:2 100:1
    48h47 full eval max, not fl-wise              -=  13/14   vs14: 74/1 289/36  u168m33  vs11-OzBVM: 78/1 319/28   u202m20 20:71 30:52 40:35 50:22 60:12 70:3 80:1 90:1 100:0
    48h48 =48h46 + bestOppMove covering my target pos (rem in 48h45) back but in changed form
                                                   +  12/18   vs14: 73/3 283/32  u170m40  vs11-OzBVM: 79/0 322/29.  u184m55 20:71 30:49 40:32 50:19 60:12 70:7 80:5 90:2 100:1 
    48h49 calc covering my target pos per oppMove, not just best., but reduced
                                                  -+  13/18   vs14: 73/3 289./33.         vs11-OzBVM: 79/0 320/27           20:71 30:49 40:32 50:19 60:12 70:7 80:5 90:2 100:1
    48h50 like 48h49, but not reduced                 12/19   vs14: 73/3 286/38.          vs11-OzBVM: 79/0 324./25.(12x) 
    
    48h51a changes to traps incl. fix of attackViaPosTowardsHereMayFallVictimToSelfDefence()
                                                  --- 18/14   vs14: 75/2 325/21(4x)       vs11-OzBVM: 79/1 343/26(4x)
    48h51b - undo trap changes, just fix atta.    =+  15/19   vs14: 72/5 286/35       vs11-OzBVM: 79/1 321/27.(6x)
    48h51c + early return in trapMethod               12/20   vs14: -                 vs11-OzBVM: 79/1 321/34.(2x)
    48h51d + coverOrAttackDistance for dist           14./18  vs14: 73/4 286/34       vs11-OzBVM: 79/1 331/23(4x)
    48h51e + noGo@d==1 cases                          15/18   vs14: 73/4 288/36.      vs11-OzBVM: 79/0 325./26
    48h51f + more reduction if NoGo + contrib for covering  
                                                  ++  15/18   vs14: 73/4 281./35.     vs11-OzBVM: 79/0 323./26.
    48h51g + getShortestReasonablePredecessorsAndDirectAttackVPcs for trap attackers attacking position
                                                 ---  18/18   vs14: 79/1 324/25       vs11-OzBVM: 80/0 348/17. 20:74 30:49 40:22 50:12 60:8 70:5 80:2 90:2 100:1
    48h51h + fee enabling trap by moving out of way---21/14   vs14: 79/0 320/27.      vs11-OzBVM: 80/0 348/17. 20:76 30:51 40:26 50:15 60:9 70:6 80:4 90:3 100:2
    48h51i tries reducing cond. traps                 21/14   vs14: -                 vs11-OzBVM: 79/0 349./14(2x)
    48h51j -g +getShortestReasonablePredecessors      21/10   vs14: 74/5 300/31       vs11-OzBVM: 79/1 337./20
    48h51k -gj +getShortestReasonableUncondPred       11/19   vs14: 73/5 285/36.      vs11-OzBVM: 78/1 328/27 
    48h51l -gj +getShortestReasonableUncondPredAndDirectAttackVPcs
                                                      20/10   vs14: 76/3 315/28.(6x)  vs11-OzBVM: 80/0 341./22.(6x) 
    48h51m getShortestReasonableUncondPredAndDirectAttackVPcs, except pawns: only directAttackVPces and d==2
                                                      17/11   vs14: 76/4 317/25       vs11-OzBVM: 80/0 344./20
    48h51n getDirectAttackVPcs, but much tighter filter for trapping cases     
                                                      15/17   vs14: 70/5 282/37.      vs11-OzBVM: 78/1 318./28     ++
           lichess_db_puzzle_230601_410-499-mateIn1.csv, AvoidMateIn1, NOTmateIn1.csv:  120, 1622, 575 failed,  35/157

    48h52 block move to mateing pos and co-coverers   16/22   vs14: 70/5 290./35      vs11-OzBVM: 78/1 314/29.  -2      23. Feb 17:48 
           lichess_db_puzzle_230601_410-499-mateIn1.csv, AvoidMateIn1, NOTmateIn1.csv:  119, 1632, 574 failed,  35/157
    48h52b more benefit f. blocking co-cov.:defend+blockBen/16
                                                      16/22   vs14: 69/5 282./33      vs11-OzBVM: 78/1 324/28   -4      23. Feb 21:34
    48h52c even less (defend-10)                      16/22   vs14: 71/4 288./36      vs11-OzBVM: 78/1 318./27. -5.     23. Feb 21:40
    48h52d (blockBenefit+10) /8 when already upper hand + /2 for block move to mateing pos              
                                                      15/21   vs14: 71/4 284/33.      vs11-OzBVM: 78/1 315./27. +3      24. Feb 00:39
    48h52e without block mateCoCoverer                15/22   vs14: 71/4 285./34      vs11-OzBVM: 78/1 318/27   -1      24. Feb 00:42
    48h52f 52d + (defendBen+10) /2 resp /8 when already upper hand + /2 for block move to mateing pos              
                                                      13/23   vs14: 70/5 284/34.      vs11-OzBVM: 78/1 316/29   +1. 
    48h52g exclude coverageDelta<-3 + defend+blockBen/32+100
                                                      15/21   vs14: 69/5 280./36      vs11-OzBVM: 78/1 318/30   +2   
    48h52h upper hand always /8 (no /2 for delta==-1) 15/21   vs14: 69/5 280./38.     vs11-OzBVM: 78/1 316./27  +3 +++

    48h53a fix isFriend color compare for contribBlk  11/16   vs14: 70/5 282./35      vs11-OzBVM: 77/2 314/27.  +2 (comp with 52h) 
    48h53b fl+1 for !isFriend in  contribBlk          12/14   vs14: 70/5 285./35.     vs11-OzBVM: 76/3 315./29. -

    48h54 0 mobility if square notOk                    -     vs14: -                 vs11-OzBVM: 78/1 323/25.  -- 
    48h54b like 54 but generating mobility if Ok       9/27   vs14: 74/4 286/34.      vs11-OzBVM: 77/3 317/30   -

    48h55 48h53a +fix addChances2PieceThatNeedsToMove 14/13   vs14: 69/5 281/33.      vs11-OzBVM: 77/1 315./30  +4.5 (comp to 53a)
    48h55b full needToMove benefit (no more *0.87)    13/15   vs14: 70/4 274./36      vs11-OzBVM: 76/2 319./27. & 312.3/30  
    48h55c needToMove benefit now *0.93               12/15   vs14: 70/4 279./36      vs11-OzBVM: 77/1 316/29   u325m46/5425plys =7.20s/2plys 20:72 30:52 40:36 50:22 60:16 70:8 80:5 90:1 100:1

    48h56 48h55b + remove doubled contribBlocking     11/17   vs14: 73/4 274/36.      vs11-OzBVM: 77/2 315./28. u303m37/5461plys =6.68s/2plys 20:74 30:54 40:35 50:20 60:15 70:9 80:5 90:1 100:1 
    48h56b full contribBlocking (no more *0.87)       11/17   vs14: 73/4 276./36      vs11-OzBVM: 77/2 317./27. -

    48h57a-d 48h56(not b)+ v1 new addMoveAwayChances()60/15   vs14 + vs11-OzBVM: almost 400:0...
    48h57e fixed accid. deleted line for relEvals ;*} 11/17   vs14: __      vs11-OzBVM: 77/2 310/31.
    48h57f addMoveAwayChances after aggregate         14/21   vs14: 79/1 294./32      vs11-OzBVM: 79/1 328./24
    48h57g 57e but using getEvalAt(inFutureLevel)     11/26   vs14: 72/4 283/38.      vs11-OzBVM: 77/2 322/29.
    48h57h improve addChances2PieceThatNeedsToMove()  11/22   vs14: 72/6 286/36.      vs11-OzBVM: 79/1 318./29.
    48h57i 57h, but not 57g = back to relEval          8/14   vs14: 75/3 273./35.     vs11-OzBVM: 78/2 316/30 = u235m26/5012plys =5.63/2plys  20:72 30:54 40:32 50:17 60:11 70:5 80:2 90:1 100:1
    48h57j test only for d==1                          9/18   vs14: 78/1 274/37       vs11-OzBVM: 79/1 311/28.

    48h57k 57i + test without -omaxbenefits           12/16   vs14: 75/4 303/33.      vs11-OzBVM: 79/1 335/26. ---
    48h57l test + addit. w/o +futureReturnBenefits    15/14   vs14: 71/6 297/32.      vs11-OzBVM: 79/1 327/23  -- 
    48h57m test + only w/o +futureReturnBenefits      18/26   vs14: 76/4 301./34      vs11-OzBVM: 79/1 330./27
    48h57n no klm but +futureReturnBenef for same axis 20/13  vs14: 73/6 297./27.     vs11-OzBVM: 75/4 331./22
    48h57p =i? somehow not... let's roll back         17/17   vs14: 71/7 298./34      vs11-OzBVM: 78/2 327./25
 
    48h56 for comparison:                             11/17   vs14: 73/4 274/36.      vs11-OzBVM: 77/2 315./28. u303m37/5461plys =6.68s/2plys 20:74 30:54 40:35 50:20 60:15 70:9 80:5 90:1 100:1 
    48h58a = 48h56 with only above improvements on addChances2PieceThatNeedsToMove() 
                                                       7/13   vs14: 75/3 279/37.      vs11-OzBVM: 78/2 314./31 =- seems little worse, but rules out a previously wrong case from the method.
    48h58b + just cosmetics? (comments + myPos)       14/21   vs14: 79/1 292./31      vs11-OzBVM: 79/1 326./25 --- aha :-) What went wrong here?
    48h58c 58a + simple myPos replace, no ecdd9f40     8/14   vs14: 75/3 278./36.     vs11-OzBVM: 78/2 315/30. 
    48h58d + (again) improve mapLostChances()          7/13   vs14: 75/3 278/38       vs11-OzBVM: 78/2 313./30  u246m26/5012 = 5.9/2plys 20:72 30:54 40:32 50:17 60:11 70:5 80:2 90:1 100:1
    48h58d2 d+ -sameAxisMaxBenefit                    13/15   vs14: 76/3 279./37.     vs11-OzBVM: 78/2 310/35 
    48h58d3 d2+ completed rightTrangle cases          14/17   vs14: 78/1 272./39.     vs11-OzBVM: 79/1 309/30. 
    48h58d4 improves avoidRunningIntoForks()             __   vs14: 73/7 269/44.      vs11-OzBVM: 79/1 309/30. **
    48h58e attempts (again) later addMoveAwayChances()18/17   vs14: 71/7 297.5/31     vs11-OzBVM: 78/2 331/23  ---
    48h59 d4 + prop.2ndOppMove consider. despite check11/14   vs14: 75/4 272/38.      vs11-OzBVM: 76/4 307/36  =-
    48h59b /2                                            __   vs14: 74/6 273/42       vs11-OzBVM: 77/3 305/37  =
    48h59c /2 + similar for nonChecks           vs44n:15/30   vs14: 73/7 264/40       vs11-OzBVM: 77/3 311./31 +
    48h59d /2 + similar for nonChecks/2         vs44n:13/35   vs14: 74/6 268./44.     vs11-OzBVM: 77/3 307/32 

    48h59e both not /2                          vs44n:13/33   vs14: 75/4 271/44.      vs11-OzBVM: 76/4 310./31.

    48h59e-master = 48h59(not/2) + nonChecks/2  vs44n:__/33   vs14: 73/7 267./43.     vs11-OzBVM: 79/1 305/34 
                                                              vs14: 73/7 274/37       vs11-OzBVM: 79/1 307/32.
                                                           avg:          271/40                        306/33
    ** 1st comparison now all vs44n **
    48h60c-master                                    :16/38   vs14: 71/7 269./45      vs11-OzBVM: 79/0 307/33. =- should be better with allNeighbours, but might indicate that fork detection still is to unprecise or has flaws, see todos concering fork detection vs checkfork-detection
                                                              vs14: 71/7 268/42       vs11-OzBVM: 79/0 311/31
    48h60c+e-master                                     :__   vs14: 71/6 273/43.      vs11-OzBVM: 78/1 306./33. =-
                                                                                                  78/1 308./32.
    (dropped 48h60c+d+e-master (d here =d1)          :15/35   vs14: 74/2 275/41       vs11-OzBVM: 76/3 309./32. -
                                                                                                  76/3 309./29.)
    48h60c+d2+e-master                                :9/42   vs14: 73/5 279/40.      vs11-OzBVM: 78/2 311./33. --

    48h61a 48h60c+d2+e-master + test concering 60c: just no allNeighb. for check-forks
                                                     :11/34   vs14: 75/4 278./43      vs11-OzBVM: 78/2 320./31. --
    48h61b 48h60c+d2+e-master + test concering 60c: just no allNeighb. for _other_ forks (in additionalChanceWouldGenerateForkingDanger)
                                                      11/44   vs14: 73/6 270./41.     vs11-OzBVM: 78/2 304./34 
    48h61c 48h60c+e-master(=2xAllNs) + axisComp.      13/41   vs14: 72/6 270./40      vs11-OzBVM: 78/1 308./28.
    48h61d c+ !=NONE                                     __   vs14: 74/3 270/41.(16x) vs11-OzBVM: 78/2 305./32(16x) ++
    48h61e avoidRunningIntoForks considers countAttacks13/33  vs14: 74/3 273/42       vs11-OzBVM: 79/1 309./31. -- 
    48h61f avoidForks (from mlCVars:61a+b)            19/29   vs14: 74/4 278/41       vs11-OzBVM: 79/1 311./31  --
    48h61g 61e, aRIF w/ better countAttacks for pawns:17/34   vs14: 75/2 278./39      vs11-OzBVM: 79/1 310./31. --
    48h61h + correct attackDir in additionalChanceWouldGenerateForkingDanger + eliminate simply covered forking squares in aRIF
                                                      12/34   vs14: 77/2 274/39       vs11-OzBVM: 76/3 307./31. -
    48h61i again incl. slightly improved? avoidForks   9/42   vs14: 77/2 290./34.     vs11-OzBVM: 78/2 316./29.2 --- 
    48h61j benefit forking and blocking in avoidForks  8/39   vs14: 73/5 272./41.     vs11-OzBVM: 78/2 310./33. + 
    48h61k 61j w/o avoidForks... almost same even     12/35   vs14: 77/2 272./38      vs11-OzBVM: 76/3 310/32   =
    48h61l 61j + improve 61e+g for pawns+sameValue     9/39   vs14: 74/4 275./40.     vs11-OzBVM: 78/2 307/34   =+
    48h61m half benefits for all in avoidForks         6/41   vs14: 72/5 268/43.      vs11-OzBVM: 78/2 307/31.  +
    48h61n >>2                                        11/30   vs14: 75/3 272./38      vs11-OzBVM: 77/3 307./34 
    48h61o 61m + not move away fork blocker            7/40   vs14: 72/5 275/40       vs11-OzBVM: 78/2 306./34. -
    48h61p 61m + not block fork                        8/39   vs14: 72/5 265./41.     vs11-OzBVM: 79/1 307./32  ++
    48h61q 61p + adjustBenefit: / nrOppHelpNeeded        __   vs14: 73/6 274/38.      vs11-OzBVM: 76/3 304./35.
                                                                    73/6 266./42.                 76/3 305/37   ++
    
    48h62a 61n + retry set relEvals for future attacks despite NoGos on the way, start with clashEval incl NoGos
                                                       8/39   vs14: 73/5 269./43.     vs11-OzBVM: 79/1 311./31  -
    48h62b 61q +  -"- + setRelEvals for NoGos            __   vs14: 72/7 264./44      vs11-OzBVM: 79/1 310./31. *** 
                                                                    72/7 270/40.                  79/1 309/31.  avg. comp to 61q = (+3,-3)
    48h62c allow ps approaching ps                       __   vs14: __                vs11-OzBVM: 79/1 305/34.  +
                                                                                                  79/1 307./33
    48h62d =b allow noone approaching same               __   vs14: __                vs11-OzBVM: 79/1 309./31. -
    48h62e allow all approaching same                    __   vs14: __                vs11-OzBVM: 79/1 310./33  -
    48h62f allow all approaching same if on my side of the board   __   vs14: __      vs11-OzBVM: 79/1 309/33   -
    48h62g allow mix of c+f                              __   vs14: __                vs11-OzBVM: 79/1 309./33. -
                                                                                  
    48h63a 62c + +15 if last in clash                    __   vs14: __                vs11-OzBVM: 76/4 312./32. -
    48h63b 62c + +45 if last in clash                    __   vs14: __                vs11-OzBVM: 77/3 307/34   -
    48h63c 62c + -*0.75 for "because it "lost check-abil"__   vs14: __                vs11-OzBVM: 79/1 307/32   -
    48h63d + -10 to +25 if last in clash                 __   vs14: __                vs11-OzBVM: 79/1 310/31.  -

    48h63e + corr. of pEvMoveHindersAbzugschach-detection 14/36 vs14: 73/5 266/41     vs11-OzBVM: 79/1 310./29.
           lichess_db_puzzle_230601_410-499-mateIn1.csv, AvoidMateIn1, NOTmateIn1.csv:  130, 1663, 829 failed,
           lichess_db_puzzle_230601_2k-9xx.csv;  ChessBoardTest:                        733 failed;   41 of 160 failed  
    48h63f + fix walkable4king for x-rays through king 16/34  vs14: 72/6 266./41.(6x) vs11-OzBVM: 79/1 309./35.
           lichess_db_puzzle_230601_410-499-mateIn1.csv, AvoidMateIn1, NOTmateIn1.csv:  __158, _1663, _829 failed,

    48h63fc = f with (63e and) 63c (no lic)            15/32  vs14: __                vs11-OzBVM: 78/2 310./30.
    48h63g = f with (63e)+ like 63d but avoiding opp lic14/32 vs14: 75/5 267./45.     vs11-OzBVM: 78/2 314/29. 

    48h63h + more checkBlocking for Nogo + only 1sq left __   vs14: 73/6 272./38      vs11-OzBVM: 75/3 317./29
    48h63i + h-corrected checkblocking defendBenefit   15/30  vs14: 74/6 269./42      vs11-OzBVM: 75/3 317./30 

    48h63j + more defendbenefit + Luft if lastWayOutIsUnsafe + fix nowFreed detection when checker takes unprotectedly in kings neighbourhood
           + fix isStraightMovingPawn to false if pawn is not moving at all :-) and use this fix in checkblocking around king 
                                                       16/28  vs14: 73/6 270./45      vs11-OzBVM: 79/0 312/31.
           lichess_db_puzzle_230601_410-499-mateIn1.csv, AvoidMateIn1, NOTmateIn1.csv:  152, 1672, 827 failed,
           lichess_db_puzzle_230601_2k-9xx.csv;  ChessBoardTest:                        718 failed;   41 of 161 failed  

    48h63k reduces 63h/i again                         14/31  vs14: 73/7 273./41      vs11-OzBVM: 78/1 311./33
    48h63l reactivate contrib 4 covering trapping pos.   __   vs14: __                vs11-OzBVM: 77/1 313/27.
    48h63m 62c (no 62d-g, no 63a-d,g, l) +63e+f +63k   11/37  vs14: 73/6 268/42       vs11-OzBVM: 79/1 309/35.  ++  u256m29/5231  20:75 30:52 40:33 50:21 60:12 70:7 80:3 90:3 100:1
    48h63n 62c (no 62d-g, no 63a-d,g, no 63l+k) +63e+f  9/39  vs14: 75/4 268./42      vs11-OzBVM: 79/1 310./35
           lichess_db_puzzle_230601_410-499-mateIn1.csv, AvoidMateIn1, NOTmateIn1.csv:  153, 1678, 827 failed,
           lichess_db_puzzle_230601_2k-9xx.csv;  ChessBoardTest:                        723 failed;   39 of 160 failed  
    
    47t22 for comparison, was >1500  already in 2023-09 28/30 vs14: 76/3 286./42      vs11-OzBVM: 77/3 336/23 
           lichess_db_puzzle_230601_410-499-mateIn1.csv, AvoidMateIn1, NOTmateIn1.csv:  482, 2015, 565 failed
            lichess_db_puzzle_230601_2k-9xx.csv:                                        723 failed

    48h64 corrected x-ray 2nd row attacker identific.   9/40  vs14: 73/6 262/42       vs11-OzBVM: 79/1 309/34. ++
           lichess_db_puzzle_230601_410-499-mateIn1.csv, AvoidMateIn1, NOTmateIn1.csv:  129, 1662, 826 failed,
           lichess_db_puzzle_230601_2k-9xx.csv;  ChessBoardTest:                        720 failed;   39 of 161 failed  
    48h64b choose mating over avoiding mate :-)                                      -> mI1:119f
    48h64c take only king color when blocking mating square by moving out of the way -> mI1:117f
                                                        8/40  vs14: 73/6 271./42      vs11-OzBVM: 79/1 303/34. -/+
    48h64d 1/2 (not 3/4) for propable next best move    8/41  vs14: 72/7 265/40       vs11-OzBVM: 77/3 306/34. +/=-
    48h64e + /2 benefit at fl+1 for opponent col moving out of the way
                                                        8/42  vs14: 72/7 264/43       vs11-OzBVM: 77/3 308/35  - 
    48h64f fix old_updateRelEval so king cannot take back on protected squares (like the new functin already did)
                                                        8/40  vs14: 72/7 270/45       vs11-OzBVM: 76/4 309/31.  -> mI1:108f
    48h64g fm, but 64e reduces to fixed 10@fl+1         8/40  vs14: 72/7 265/41.      vs11-OzBVM: 76/4 303/34 +++ **

    48h65 improve fork when forking piece that can attack back
                                                        9/37  vs14: 70/7 252/45       vs11-OzBVM: 78/1 294./38.  -> mI1:105
                                                                    70/7 251/47.
    48h66 abzugschach by pawn is not considered possible if its target piece moves away
                                                        9/37  vs14: 70/7 259./42      vs11-OzBVM: 78/1 294/36   -> mI1:105, CBT:38f
                                                                    70/7 250./47.
    48h67 h65 (not h66) + fix oppMoveIsStillCheckGiving 9/37  vs14: __                vs11-OzBVM: 78/1 292./37 
    48h67b like a h65b+67: no bad chances for forks    11/36  vs14: 69/9 255/47.      vs11-OzBVM: 75/3 291./37.  
    48h67c 67b+no forks for already threatened sqs     10/36  vs14: 68/10 251./49     vs11-OzBVM: 77/1 290/38.   +
    48h67d +h66                                        10/36  vs14: 68/10 251/49      vs11-OzBVM: 77/1 294./39
    (   48h67e +further avoid fork improvements            11/33  vs14: 68/9 262./43.     vs11-OzBVM: 78/2 306.6/36.6 
                lichess_db_puzzle_230601_410-499-mateIn1.csv, AvoidMateIn1, NOTmateIn1.csv:  109, 1651, _826 failed,
                lichess_db_puzzle_230601_2k-9xx.csv;  ChessBoardTest:                        _720 failed;   34 of 163 failed  
        48h67f corrected 67e                               11/33  vs14: 68/9 262/42(4x)   vs11-OzBVM: 78/2 302./36
                                                                        68/9 259/45(8x)               78/2 303./33.
        48h67g =c?                                         10/36  vs14: 68/10 252./45.    vs11-OzBVM: 77/1 298./35.

       48h67h tried -sign for danger                      14/34  vs14: 74/4 260/44.      vs11-OzBVM: 77/3 305/32. -- 
        48h67i -sign back (via additionalChance) + *0.75   12/38  vs14: 65/12 256./47.    vs11-OzBVM: 77/2 298/35
        48h67j fix attackerAtLMOIsProtected                12/38  vs14: 65/12 258/44.     vs11-OzBVM: 77/2 295./36 
        48h67k no cOAD==2 comp, + loose forker if vPce is covered
                                                           13/30  vs14: 68/9 252/46       vs11-OzBVM: 76/2 287./40. ++
        48h68                                              13/30  vs14: 68/9 247/52.      vs11-OzBVM: 76/2 292./39.
        48h68b still dont't get it.. try -danger again:    12/33  vs14: 70/9 266./44.     vs11-OzBVM: 79/1 299./33  ?? why worse - w/o -danger it is incorrectly evaluating forked piece
        48h69 68b, but correction of moveorigin passing to sliding neighbours VSPoS:467  
                                                           12/28  vs14: 70/9 262/45       vs11-OzBVM: 79/0 299/36.   CBT:33f/163 mI1:88f aMI1:1666f
                                                                        70/9 259./45                  79/0 302./36.
        48h70c found reason? from games80-delta, 70+exceptPos 11/31 vs14:69/10 250./46.   vs11-OzBVM: 79/0 296/37   +
    )
    48h70d and 2nd reason :-) think of killable forkers 12/29 vs14: 71/8 252./42.     vs11-OzBVM: 79/1 290./40. +  **
    48h71 fix clashContrib when only king attacks piece 10/30 vs14: 69/9 243/41       vs11-OzBVM: 79/1 291/32   + 
    48h72 fix conquering sq benefit for black pieces   18/28  vs14: 67/7 247/39.      vs11-OzBVM: 78/2 286./32. =+
    48h73 limiting future attack on king to 10th       16/32  vs14: 66/10 252./39.    vs11-OzBVM: 77/3 284./35. -
    48h73b 73+72b reduced amount of ableToTakeControlBonus, but additionally also for overprotection and empty squares not around the king
                                                       10/35  vs14: 68/6 242./47.     vs11-OzBVM: 77/3 285/34.  +
    (48h74 73b + fee neutral taking if not up in pieces  8/41  vs14: 73/3 251/39.(4x)  vs11-OzBVM: 78/1 288./34. -)
    48h73c like 73b but comp to 73 higher limit to HALF_A_PAWN + but also limit it for later future chances
                                                        9/41  vs14: 68/6 243./43      vs11-OzBVM: 78/1 291/34. +=  +++
    48h73d repairs broken defending the forking square  7/40  vs14: 69/6 241./43.     vs11-OzBVM: 76/3 291./31 =+ --> CBT:31f/164 mI1:88f
    48h74 add abzugcoverers on kings escape squares     8/39  vs14: 70/5 240/39.      vs11-OzBVM: 77/2 290/35  =+ **  --> CBT:29f/164 mI1:87f
    48h75 unfinished(!) reallyHinders vs. moreOrLessH  12/27  vs14: 71/4 248/40       vs11-OzBVM: 77/3 290/35. -
    48h75b finished reallyHinders +fix? max-bestOppMove 6/37  vs14: 71/5 243/47       vs11-OzBVM: 77/2 291/37.
    48h75c w/o reallyHinders vs. moreOrLessH, just fix  8/36  vs14: 70/6 244./44      vs11-OzBVM: 77/2 286./35(16x) 
    48h75d 75b + corr. pawn moving away in hinders      6/37  vs14: 71/5 249./40.     vs11-OzBVM: 77/2 290./33. - 
    48h75e reallyHinderingin pEvMoveHindersOrNoAbzug    6/37  vs14: 71/5 250./37.u190 vs11-OzBVM: 77/2 287/38 u243m13/5793 20:78 30:58 40:37 50:21 60:12 70:9 80:5 90:5 100:3 =+ 
    48h75f no early break in oppMove selection          8/32  vs14: 70/5 247./40.     vs11-OzBVM: 77/2 289/33 u221m27/5679
    48h76 = 48h75g = f w/o e                            8/32  vs14: 70/5 243./37.     vs11-OzBVM: 77/2 284/32. + ***

    48h77 (re)allow benefit for blocking too late       8/32  vs14: 70/5 241/45.      vs11-OzBVM: 77/2 282/36 -=
    48h77b only when just too late                      8/32  vs14: 70/5 250./39      vs11-OzBVM: 77/2 291/35 -- 
    48h77c =76? (>>3+delta plays no role)               8/32  vs14: 70/5 245./41.     vs11-OzBVM: 77/2 283/36 
    48h78 benefit for blocking pawns promotion square + more benefit for blocking esep. if close to promotion
                                                        7/31  vs14: 70/5 242./43      vs11-OzBVM: 77/2 282./35. +
    48h78b more benefit against pawn promo              8/26  vs14: 69/5 244/44       vs11-OzBVM: 78/2 290./31
    48h78c benefit all closest not just 1               6/31  vs14: 68/7 255/38.      vs11-OzBVM: 78/1 297./30 --
                                                                    68/7 246./40
    48h78d 78c but less benefit again                   6/32  vs14:                   vs11-OzBVM: 77/1 286./34.
    48h78e 78d and all closest only for pawndist==0     __    vs14: 70/5 244./41.     vs11-OzBVM: 77/2 289./31.
    
    48h79 reenabled broken "too late but anyway"       11/29  vs14: 70/7 246/42       vs11-OzBVM: 73/7 288/35 =- 
    48h79f (79+78f) rereduce pawn-defending-benefit     9/26  vs14: 72/5 248/39.      vs11-OzBVM: 76/2 286./33 =+ 
    48h79g reduce non-urgent pawn blocking              9/25  vs14: 72/5 237./45.     vs11-OzBVM: 75/3 287./32. + bzw =- zu 75f
                                                                    72/5 243/41.                  75/3 288/35.
    48h79h less benefit in addBenefitToBlockers for beeing too late.
                                                        7/27  vs14: 71/5 239./42.     vs11-OzBVM: 75/3 287/33 =-
    48h79i less benefit for being too late in addChance corrected to become less and less with increasing distance
                                                        7/31  vs14: 70/6 248/41       vs11-OzBVM: 76/2 292/31 --
    48h79j 79h but corr. to become less with inc dist.  8/33  vs14: 72/4 239/42       vs11-OzBVM: 76/2 291./31.
    48h79k 79j but benefit multiple pawn defenders only if countReasonableTakers == 0
                                                        8/33  vs14: 72/4 245.5/40     vs11-OzBVM: 76/2 284./36 
    48h79l pawn-promo reworked to FL (instead of dist) 11/22  vs14: 72/4 258./25.     vs11-OzBVM: 76/1 293./28 -- 
    48h79m fix so pawnFL cannot become neg.            10/22  vs14: 72/5 251/33.      vs11-OzBVM: 77/0 298/24. 
                                                                    72/5 255/29.                  77/0 286./29
    48h79n test pawn dists are bad, restrict pawnFL<=2  8/36  vs14: 70/6 255./53      vs11-OzBVM: 75/5 296/40
    48h79o test pawn dists are bad, restrict pawnFL<=3  8/31  vs14: 74/3 256./41      vs11-OzBVM: 76/2 300/34
    48h79p restrict only for taking, not for pawn itslf 8/24  vs14: 72/5 250/33       vs11-OzBVM: 76/2 294/27 

    cherry-pick from 80*: (letter should start at q not l...)
    48h79l +scrollbar ;-) + refact movingAwayDistPenalty() + use movingAwayDistPenalty() for piece blocking a double-square pawn move
                                                       12/35  vs14: 69/8 242./41.     vs11-OzBVM: 76/4 286./32. ++
    48h79m fix recalcSquarePawnDistance() for rare case of unset suggestionTo1HopN where relEval requires propagatio
                                                       10/32  vs14: 66/8 245/41       vs11-OzBVM: 77/2 289/35  -=
    48h79n corrects comparison of ConditionalDistances 10/29  vs14: 68/7 247./41      vs11-OzBVM: 80/0 288/33  =

    48h79q change pawn distance calculation            13/28  vs14: 77/2 255/38.      vs11-OzBVM: 80/0 301/33. -- however, pawn dist is now corrected... looking for reasons:

    48h82a 79q +exclude NoGos from additional attackrs 11/29  vs14: 73/5 254./42      vs11-OzBVM: 80/0 300./31 += but not compensating the problems of 79q
        same ver. on arena docker cont. in BodhiVMonOz   -    vs14: vs14: 73/5 259/38       vs11-OzBVM: 80/0 296./31(24x)

    48h80a fix pawnRelEval when moving straight on pce  7/28  vs14: 73/5 252./31      vs11-OzBVM: 75/3 293/25

    48h80b w/o restrict <=3 for taking                  7/25  vs14: 73/5 252/31.      vs11-OzBVM: 76/1 290./31 
    48h80c try always minimal benefit when nogo         7/25  vs14: 73/5 250/31.      vs11-OzBVM: 76/1 294./28 

    48h81a repairs / restricts pawn dists              16/22  vs14: 71/2 267/25       vs11-OzBVM: 75/5 307/23.
    48h81c                                             15/27  vs14: 74/4 267/29       vs11-OzBVM: 80/0 301./26 
    
    48h82a pawn correction corrupt, no recalc after mve 11/29 vs14:73/5 254./42 u221m but 25x****Fehler   vs11-OzBVM: 80/0 300./31 u294 but 42****Fehler
                                                              vA14:73/5 259/38        vA11: 80/0 298/28.
    48h82b corrected but permanent recursive recalcs.. 14/34  vs14: 70/6 249/38. u855m (!!) no****Fehler   vs11-OzBVM: 75/3 285/32. u732 but no****Fehler
    48h82c precalc in recalcAllPawnDists for d==0      18/29  vs14: __      vs11-OzBVM: 74/4 298/32.  u704m

    48h82d                                             16/30  vs14: 72/3 256./35(2x)  vs11-OzBVM: 75/3 298./29 u698m
    48h82e                                             17/39  vs14: 73/2 259./31(2x)  vs11-OzBVM: 75/3 296/30 
    48h82f                                             13/33  vs14: 71/5 245./40.u250m vs11-OzBVM:75/3 285./34 u248m
    48h82f same, but test in local container           13/33  vA14: 71/5 242/40       vA11-OzBVM:75/3 290./26 u247m
           lichess_db_puzzle_230601_410-499-mateIn1.csv, AvoidMateIn1, NOTmateIn1.csv:  100, 1664, 738 failed,
           lichess_db_puzzle_230601_2k-9xx.csv;  ChessBoardTest:                        655 failed;   32 of 164 failed  
    48h82g (ran as 82a..) minDist.NoGo in Sq.pnExtraBen 13/33 vs14: 71/5 249./36.     vs11-OzBVM: 75/3 287/35.
           same in test container on OzBVM              __    vA14: 71/6 247./41.     vs11-OzBVM: 75/3 289./34 u241m /5589moves  Length: 20:74 30:55 40:33 50:24 60:18 70:9 80:6 90:2 100:2 

    48h83  accidently change deactivated ;-D -> ==82g    __   vs14: 71/6 244./38.     vs11-OzBVM: 77/1 293/38(2x) ??
    48h83a let straight pawns be the first "assassin"  14/33  vs14: 71/7 253/36       vs11-OzBVM: 79/0 291/36
           same in test container on OzBVM             14/33  vA14: 71/7 250./42.     vs11-OzBVM: -

    48h84a improve? pawn promotion + defense           17/39  vA14: 79/1 307./34.     vs11-OzBVM: 78/2 306/38. -
    48h84b still 84a, but disable 2xfix Sq:752           __   vs14: __      vs11-OzBVM: 79/1 307./34.
    48h84c check reasonablyKillableOnTheWay for promo  18/39  vs14: __      vs11-OzBVM: 80/0 306./39

    48h85a benefit for taking/attacking pce w/ contrib 21/38  vA14: 72/4 267/46.      vs11-OzBVM: 78/1 307/35.

    48h85b 83a+85a (w/o 84*)                           12/30  vs14: 72/6 251/33.(3x)  vs11-OzBVM: ? 295/36(4x)
    48h85c pawn !killable and no late takes             9/27  vA14: 74/4 245/34.(6x)  vs11-OzBVM: 80/0 291./25

    48h51x                                             __   vs14: __      vs11-OzBVM: __


    --- from branch mapLostChances-Variants: ---
        48h59f h59 (not /2) + nonChecks/2           vs44n:11/35   vs14: 75/4 269./45.     vs11-OzBVM: 76/4 305/30. ++ 
        48h59g h59f +min nonChecks to getBestMoveRelEval :11/32   vs14: 75/4 272./41.     vs11-OzBVM: 77/3 308/33. -
        48h59h h59g but switched col for min                __    vs14: 74/5 269./43      vs11-OzBVM: 76/4 304./33. u215m56  +++
    
        48h60a incorrect attempt to improve forks        :16/30   vs14: 74/5 270/41. u167m58  vs11-OzBVM: 78/2 313./32  u206m26
                                                                  vs14: 74/5 278./39 u167m13  vs11-OzBVM: 78/2 310./29. u205m5
                                                                                              vs11-OzBVM: 78/2 306./31. u239m33
        48h60b improved (check+general) forks w/ getAllNeighbours
                                                         :11/38   vs14: 80/0 280/39  u170/12  vs11-OzBVM: 78/1 309./33 u244m4
        48h60c same as 60b, but cached allNeighbours     :11/38   vs14: 80/0 278./40.u171m19  vs11-OzBVM: 78/1 306/31. u254m33 
        48h60d refactored avoidRunningIntoForks          :13/40   vs14: 80/0 275/41           vs11-OzBVM: 78/1 311./32 
        48h60e refactored/added contribToDefendersByColor:14/40   vs14: 79/1 274./40          vs11-OzBVM: 77/2 306./32. 
        
        48h61a add avoidForks (complem. avoidRunningInto):14/40   vs14: 74/5 276./38.         vs11-OzBVM: 75/4 312/32
        48h61b some corrections to avoidForks               :__   vs14: __      vs11-OzBVM: __ 
        48h61c relEval: coverOrAttackDistance NOT Nogofree  :__   vs14: __      vs11-OzBVM: __ 
        48h61d finished relEval for future clashes       :16/35   vs14: 74/4 291./36      vs11-OzBVM: 79/1 323./30  -- 
        48h61e w/o avoidForks, just relEval-changes      :16/38   vs14: 77/3 287/37       vs11-OzBVM: 79/1 321/30.  + 

    48h51x                                            __   vs14: __      vs11-OzBVM: __ 
