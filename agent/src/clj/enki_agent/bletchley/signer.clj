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
(ns enki-agent.bletchley.signer
  "Useful functions for signing and reading/writing objects"
  (:require [clojure.java.io :as io])
  (:import (net.lshift.spki.suiteb Action
                                   PrivateSigningKey
                                   Sequence
                                   SequenceItem
                                   Signed)
           (net.lshift.spki.suiteb.simplemessage SimpleMessage)
           (net.lshift.spki.convert.openable FileOpenable OpenableUtils)
           (net.lshift.spki.convert ConverterCatalog)))

(defn generate-signing-key
  ^PrivateSigningKey
  []
  (PrivateSigningKey/generate))

(defn sign-item
  [^PrivateSigningKey signing-key item]
  (let [signature      (.sign signing-key item)
        signature-info (Signed/signed item)]
    (Sequence. [signature signature-info])))

(defn sign-assertion
  [assertion signing-key]
  (let [action-item    (Action. assertion)]
    (sign-item signing-key action-item)))

(defn write-object-file
  "Serializes any object that Bletchley knows how to convert to a binary file.
   That is class instances with @Convert.* annotation"
  [filename item]
  (let [file     (io/file filename)
        openable (FileOpenable. file)]
    (OpenableUtils/write openable item)))

(defn read-object-file
  "Reconstructs the given class instance from a binary file. Opposite of write-object"
  [filename catalog clazz]
  (let [file     (io/file filename)
        openable (FileOpenable. file)]
    (OpenableUtils/read catalog clazz openable)))
