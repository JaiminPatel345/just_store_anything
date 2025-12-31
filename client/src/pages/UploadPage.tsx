import React, { useCallback, useState } from 'react';
import { useDropzone } from 'react-dropzone';
import { useDispatch, useSelector } from 'react-redux';
import type { AppDispatch, RootState } from '../store';
import { uploadFile, resetState } from '../store/fileSlice';
import { UploadCloud, File, X, CheckCircle, Loader2, AlertTriangle } from 'lucide-react';
import clsx from 'clsx';
import { motion, AnimatePresence } from 'framer-motion';

const UploadPage: React.FC = () => {
  const dispatch = useDispatch<AppDispatch>();
  const { uploading, uploadSuccess, error } = useSelector((state: RootState) => state.file);
  const { isAuthenticated } = useSelector((state: RootState) => state.auth);
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [encryptFile, setEncryptFile] = useState(false);
  const [secretKey, setSecretKey] = useState('');

  const onDrop = useCallback((acceptedFiles: File[]) => {
    if (acceptedFiles?.length > 0) {
      setSelectedFile(acceptedFiles[0]);
      dispatch(resetState());
    }
  }, [dispatch]);

  const { getRootProps, getInputProps, isDragActive } = useDropzone({
    onDrop,
    multiple: false,
    disabled: uploading || !isAuthenticated
  });

  const handleUpload = () => {
    if (!isAuthenticated) {
      alert("Please connect your YouTube account first.");
      return;
    }
    if (selectedFile) {
      if (encryptFile && !secretKey) {
          alert("Please enter a secret key.");
          return;
      }
      dispatch(uploadFile({ file: selectedFile, secretKey: encryptFile ? secretKey : undefined }));
    }
  };

  const removeFile = (e: React.MouseEvent) => {
    e.stopPropagation();
    setSelectedFile(null);
    dispatch(resetState());
    setEncryptFile(false);
    setSecretKey('');
  };

  return (
    <div className="max-w-3xl mx-auto">
      <div className="mb-8 text-center">
        <h1 className="text-3xl font-bold text-white mb-2">Upload Files</h1>
        <p className="text-gray-400">Convert your files into video format for secure storage.</p>
      </div>

      {/* Auth Warning */}
      {!isAuthenticated && (
        <motion.div
          initial={{ opacity: 0, y: -10 }}
          animate={{ opacity: 1, y: 0 }}
          className="mb-6 p-4 rounded-lg bg-yellow-500/10 border border-yellow-500/30 flex items-center gap-3"
        >
          <AlertTriangle className="w-5 h-5 text-yellow-500 flex-shrink-0" />
          <div>
            <p className="text-yellow-400 font-medium">YouTube Not Connected</p>
            <p className="text-yellow-400/70 text-sm">Please connect your YouTube account above to upload files.</p>
          </div>
        </motion.div>
      )}

      <div className="space-y-6">
        <div
          {...getRootProps()}
          className={clsx(
            'relative border-2 border-dashed rounded-xl p-12 transition-all duration-300 ease-in-out cursor-pointer',
            isDragActive ? 'border-blue-500 bg-blue-500/10' : 'border-gray-700 hover:border-gray-500',
            (uploading || !isAuthenticated) ? 'opacity-50 cursor-not-allowed' : '',
            'bg-[#242424]'
          )}
        >
          <input {...getInputProps()} />
          <div className="flex flex-col items-center justify-center text-center space-y-4">
            <div className={clsx(
              "p-4 rounded-full bg-gray-800 transition-transform duration-300",
              isDragActive ? "scale-110" : ""
            )}>
              <UploadCloud className="w-10 h-10 text-blue-400" />
            </div>
            <div>
              <p className="text-lg font-medium text-white">
                {isDragActive ? 'Drop your file here' : 'Drag & drop your file here'}
              </p>
              <p className="text-sm text-gray-500 mt-1">or click to browse</p>
            </div>
          </div>
        </div>

        <AnimatePresence>
          {selectedFile && (
            <motion.div
              initial={{ opacity: 0, y: 10 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: -10 }}
              className="bg-[#2a2a2a] rounded-lg p-4 transition-colors border border-gray-700 hover:border-gray-600"
            >
              <div className="flex items-center justify-between mb-4">
                <div className="flex items-center space-x-4 overflow-hidden">
                    <div className="p-2 bg-blue-500/10 rounded-lg">
                    <File className="w-6 h-6 text-blue-400" />
                    </div>
                    <div className="truncate">
                    <p className="text-sm font-medium text-white truncate">{selectedFile.name}</p>
                    <p className="text-xs text-gray-500">{(selectedFile.size / 1024 / 1024).toFixed(2)} MB</p>
                    </div>
                </div>
                {!uploading && !uploadSuccess && (
                    <button
                    onClick={removeFile}
                    className="p-1 hover:bg-gray-700 rounded-full text-gray-400 hover:text-white transition-colors"
                    >
                    <X className="w-5 h-5" />
                    </button>
                )}
              </div>

               {!uploadSuccess && (
                  <div className="border-t border-gray-700 pt-4">
                    <div className="flex items-center space-x-3 mb-3">
                         <input
                            type="checkbox"
                            id="encryptFile"
                            checked={encryptFile}
                            onChange={(e) => setEncryptFile(e.target.checked)}
                            className="w-4 h-4 text-blue-600 rounded focus:ring-blue-600 ring-offset-gray-800 bg-gray-700 border-gray-600"
                         />
                         <label htmlFor="encryptFile" className="text-sm font-medium text-gray-300 cursor-pointer select-none">
                            Encrypt File
                         </label>
                    </div>
                    
                    <AnimatePresence>
                        {encryptFile && (
                            <motion.div
                                initial={{ opacity: 0, height: 0 }}
                                animate={{ opacity: 1, height: 'auto' }}
                                exit={{ opacity: 0, height: 0 }}
                                className="overflow-hidden"
                            >
                                <input
                                    type="password"
                                    placeholder="Enter Secret Key"
                                    value={secretKey}
                                    onChange={(e) => setSecretKey(e.target.value)}
                                    className="w-full px-3 py-2 bg-[#1a1a1a] border border-gray-700 rounded-md text-white text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent placeholder-gray-500"
                                />
                            </motion.div>
                        )}
                    </AnimatePresence>
                  </div>
               )}
            </motion.div>
          )}
        </AnimatePresence>

        <div className="flex justify-end">
          <button
            onClick={handleUpload}
            disabled={!selectedFile || uploading || uploadSuccess || !isAuthenticated}
            className={clsx(
              "px-6 py-2.5 rounded-lg font-medium transition-all duration-200 flex items-center space-x-2",
              !selectedFile || uploading || uploadSuccess || !isAuthenticated
                ? "bg-gray-800 text-gray-500 cursor-not-allowed"
                : "bg-blue-600 hover:bg-blue-700 text-white shadow-lg shadow-blue-500/20"
            )}
          >
            {uploading ? (
              <>
                <Loader2 className="w-5 h-5 animate-spin" />
                <span>Processing...</span>
              </>
            ) : uploadSuccess ? (
              <>
                <CheckCircle className="w-5 h-5" />
                <span>Uploaded</span>
              </>
            ) : (
              <>
                <UploadCloud className="w-5 h-5" />
                <span>Start Upload</span>
              </>
            )}
          </button>
        </div>

        <AnimatePresence>
          {error && (
            <motion.div
              initial={{ opacity: 0, height: 0 }}
              animate={{ opacity: 1, height: 'auto' }}
              exit={{ opacity: 0, height: 0 }}
              className="mt-4 p-4 rounded-lg bg-red-500/10 border border-red-500/20 text-red-400 text-sm"
            >
              Error: {error}
            </motion.div>
          )}

          {uploadSuccess && (
            <motion.div
              initial={{ opacity: 0, height: 0 }}
              animate={{ opacity: 1, height: 'auto' }}
              exit={{ opacity: 0, height: 0 }}
              className="mt-4 p-6 rounded-lg bg-green-500/10 border border-green-500/20 text-green-400 text-sm"
            >
              <h3 className="text-lg font-semibold mb-2 flex items-center">
                <CheckCircle className="w-5 h-5 mr-2" />
                Upload Successful
              </h3>
              <p>Your file has been converted to video.</p>
              <div className="mt-4 p-4 bg-black/40 rounded border border-gray-700 font-mono text-xs">
                 <p className="text-gray-400 mb-1">Temporary Server Path:</p>
                 <code className="text-blue-300">/tmp/jaimin.mp4</code>
              </div>
              <p className="mt-2 text-gray-400 text-xs">
                You can retrieve this file on the Retrieve page using the path above.
              </p>
            </motion.div>
          )}
        </AnimatePresence>
      </div>
    </div>
  );
};

export default UploadPage;
