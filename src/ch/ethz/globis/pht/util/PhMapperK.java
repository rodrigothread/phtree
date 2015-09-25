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

/**
 * Type of mapper that does not use the value of the PHEntry, only the key.
 */
public interface PhMapperK<T, R> extends PhMapper<T, R> {

    static <T> PhMapperK<T, long[]> LONG_ARRAY() {
        return e -> (e.getKey());
    }

    static <T> PhMapperK<T, double[]> DOUBLE_ARRAY() {
        return e -> (toDouble(e.getKey()));
    }

    static double[] toDouble(long[] point) {
        double[] d = new double[point.length];
        for (int i = 0; i < d.length; i++) {
            d[i] = BitTools.toDouble(point[i]);
        }
        return d;
    }

}