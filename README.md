# CharacterDetectionCalculatorAPP
 Developed with C/C++, Linux, Java, android, OpenCL, OpenCV, tesseract OCR
![스크린샷 2023-10-21 175045](https://github.com/ongsiru/MobileRPG/assets/99703356/415af0e3-0cac-4d48-acb4-cc58d57c87b3)

 ## 1. Embeded System
- <b>Operating System</b><br>
리눅스 커널을 통하여 작성한 드라이버 C 파일을 이종 기기에 빌드하고 Pthread로 운영체제를 구축한다.

## 2. Android
- <b>Client</b><br>
카메라로 찍은 이미지를 비트맵 형식으로 받아온 후 검출된 문자를 계산해주는 App을 작성한다.

## 3. OpenCL
- <b>Grey Scaling</b><br>
문자 검출을 돕기 위해 GPU에서 Pooling과 Image Padding을 이용하여 GreyScale 병렬연산 처리를 한다.

## 3. OpenCV
- <b>Tesseract OCR</b><br>
딥러닝 기반의 Open Source Library로 문자 데이터의 Feature를 학습하여 Prototype과 매칭한 후 문자를 검출한다.
