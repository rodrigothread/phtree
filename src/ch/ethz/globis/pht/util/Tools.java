/*
This file is part of PH-Tree:
A multi-dimensional indexing and storage structure.

Copyright (C) 2011-2015
Eidgenössische Technische Hochschule Zürich (ETH Zurich)
Institute for Information Systems
GlobIS Group
Tilmann Zaeschke
zaeschke@inf.ethz.ch or zoodb@gmx.de

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package ch.ethz.globis.pht.util;

public class Tools {
	
	public static long getMemUsed() {
		long tot1 = Runtime.getRuntime().totalMemory();
		long free1 = Runtime.getRuntime().freeMemory();
		long used1 = tot1 - free1;
		return used1;
	}
	
	public static long printMemUsed(String txt, long prev, int n) {
		long current = getMemUsed();
		System.out.println(txt + ": " + (current-prev) + "   per item: " + (current-prev)/n);
		return current-prev;
	}
	
	public static long cleanMem(int N, long prevMemUsed) {
		long ret = 0;
        for (int i = 0; i < 5 ; i++) {
	        ret = Tools.printMemUsed("MemTree", prevMemUsed, N);
        	System.gc();
        	try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
        }
        return ret;
	}
}
