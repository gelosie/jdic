@echo off
rm -r cab_test
set src=D:\JDIC\JDICplus\samples\ArcTest\src\
mkdir cab_test
echo pack test:
echo ======================
echo LZH 0,9:
call cab.cmd a -r -aLZH -l9 cab_test\test.LZH9.cab  %src% > log.txt
call cab.cmd a -r -hD -aLZH -l0 cab_test\test.LZH0.cab  %src% >> log.txt
echo    done
echo MSZIP:
call cab.cmd a -r -hD -aMSZIP cab_test\test.MSZIP.cab  %src% >> log.txt
echo    done
echo NONE:
call cab.cmd a -r -hD -aNONE cab_test\test.NONE.cab  %src% >> log.txt
echo    done
echo LZH 0,9 + crc:
call cab.cmd a -r -aLZH -l9 -Ext cab_test\test.LZH9crc.cab  %src% >> log.txt
call cab.cmd a -r -aLZH -l0 -Ext cab_test\test.LZH0crc.cab  %src% >> log.txt
echo    done
echo MSZIP + crc:
call cab.cmd a -r -aMSZIP -Ext cab_test\test.MSZIPcrc.cab  %src% >> log.txt
echo    done
echo NONE + crc:
call cab.cmd a -r -aNONE -Ext cab_test\test.NONEcrc.cab  %src% >> log.txt
echo    done
echo ______________________
echo unpack test:
echo ======================
echo LZH 0,9:
call cab.cmd x cab_test\test.LZH9.cab cab_test\LZH9 >> log.txt
call cab.cmd x cab_test\test.LZH0.cab cab_test\LZH0 >> log.txt
echo    done
echo MSZIP:
call cab.cmd x -hD cab_test\test.MSZIP.cab cab_test\MSZIP >> log.txt
echo    done
echo NONE:
call cab.cmd x -hD cab_test\test.NONE.cab cab_test\NONE >> log.txt
echo    done
echo LZH 0,9 + crc:
call cab.cmd x cab_test\test.LZH9crc.cab cab_test\LZH9crc >> log.txt
call cab.cmd x -hD cab_test\test.LZH0crc.cab cab_test\LZH9crc >> log.txt
echo    done
echo MSZIP + crc:
call cab.cmd x cab_test\test.MSZIPcrc.cab cab_test\MSZIPcrc >> log.txt
echo    done
echo NONE + crc:
call cab.cmd x cab_test\test.NONEcrc.cab cab_test\NONEcrc >> log.txt
echo    done
echo ______________________