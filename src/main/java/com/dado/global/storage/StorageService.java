package com.dado.global.storage;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {

    /**
     * 이미지 파일을 저장하고 접근 가능한 URL을 반환한다.
     *
     * @param file 업로드할 이미지 파일
     * @return 저장된 파일의 접근 URL
     */
    String upload(MultipartFile file);

    /**
     * 저장된 이미지 파일을 삭제한다.
     *
     * @param fileUrl 삭제할 파일의 URL
     */
    void delete(String fileUrl);
}
