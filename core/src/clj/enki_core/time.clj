;; This file is part of Enki.
;;
;; Copyright © 2016 - 2019 Oliver Wyman Ltd.
;;
;; Enki is free software: you can redistribute it and/or modify
;; it under the terms of the GNU General Public License as published by
;; the Free Software Foundation, either version 3 of the License, or
;; (at your option) any later version.
;;
;; Enki is distributed in the hope that it will be useful,
;; but WITHOUT ANY WARRANTY; without even the implied warranty of
;; MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
;; GNU General Public License for more details.
;;
;; You should have received a copy of the GNU General Public License
;; along with Enki.  If not, see <https://www.gnu.org/licenses/>
;;
(ns enki-core.time
  (:import (java.time Clock Instant LocalDateTime ZonedDateTime ZoneId)
           (java.time.format DateTimeFormatter)))

(def ^ZoneId UTC (ZoneId/of "Z"))

(def ^DateTimeFormatter rfc-3339-format (DateTimeFormatter/ISO_OFFSET_DATE_TIME))

(defn utc-now
  ^java.time.LocalDateTime
  []
  (LocalDateTime/now UTC))

(defn localdatettime-to-zoned
  (^java.time.ZonedDateTime [^LocalDateTime x]
   (localdatettime-to-zoned x UTC))
  (^java.time.ZonedDateTime [^LocalDateTime x ^ZoneId z]
   (ZonedDateTime/of x z)))

(defn instant->utc-datetime
  ^java.time.LocalDateTime
  [^Instant x]
  (LocalDateTime/ofInstant x UTC))

(defn to-rfc3339-string
  ^String
  [^ZonedDateTime x]
  (.format x rfc-3339-format))

(defn from-rfc3339-string
  ^java.time.ZonedDateTime
  [^String x]
  (ZonedDateTime/parse x rfc-3339-format))
