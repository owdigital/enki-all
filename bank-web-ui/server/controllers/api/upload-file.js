/*
   This file is part of Enki.
  
   Copyright © 2016 - 2019 Oliver Wyman Ltd.
  
   Enki is free software: you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.
  
   Enki is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.
  
   You should have received a copy of the GNU General Public License
   along with Enki.  If not, see <https://www.gnu.org/licenses/>
*/

import path from 'path';
import fs from 'fs';
import Busboy from 'busboy';

let service = {
  createBusboy: (headers) => {
    let b = new Busboy({ headers: headers });
    return b;
  }
};

// POST
function upload(req, res) {
  let busboy = service.createBusboy(req.headers);
  busboy.on('file', (fieldname, file, filename) => {
    const saveTo = path.join(__dirname, '../../tmp/', path.basename(filename));
    const fstream = fs.createWriteStream(saveTo);
    fstream.on('error', function(err) {
      // eslint-disable-next-line no-console
      console.error('ERROR:' + err);
      file.unpipe();
      fstream.end();
    });
    file.pipe(fstream);
  });
  busboy.on('finish', () => {
    res.writeHead(200, { 'Connection': 'close' });
    res.end('Upload finished.');
  });
  return req.pipe(busboy);
}

export default {
  upload,
  service
};
