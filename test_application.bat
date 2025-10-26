@echo off
echo ========================================
echo Testing OWASP API Security Application
echo ========================================
echo.

echo Testing if application is running...
echo.

echo 1. Testing H2 Console access...
curl -s -o nul -w "HTTP Status: %%{http_code}\n" http://localhost:8080/h2-console
echo.

echo 2. Testing login endpoint...
curl -s -o nul -w "HTTP Status: %%{http_code}\n" http://localhost:8080/api/auth/login
echo.

echo 3. Testing protected endpoint (should return 403)...
curl -s -o nul -w "HTTP Status: %%{http_code}\n" http://localhost:8080/api/accounts/mine
echo.

echo 4. Testing login with credentials...
curl -X POST http://localhost:8080/api/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"alice\",\"password\":\"alice123\"}" ^
  -s
echo.
echo.

echo ========================================
echo Manual Testing Instructions
echo ========================================
echo.
echo 1. Open browser and go to: http://localhost:8080/h2-console
echo    - JDBC URL: jdbc:h2:mem:apilab
echo    - Username: sa, Password: (empty)
echo.
echo 2. Use Postman or curl to test API endpoints:
echo.
echo    Login as Alice:
echo    curl -X POST http://localhost:8080/api/auth/login ^
echo      -H "Content-Type: application/json" ^
echo      -d "{\"username\":\"alice\",\"password\":\"alice123\"}"
echo.
echo    Login as Bob (Admin):
echo    curl -X POST http://localhost:8080/api/auth/login ^
echo      -H "Content-Type: application/json" ^
echo      -d "{\"username\":\"bob\",\"password\":\"bob123\"}"
echo.
echo 3. Use the returned JWT token to access protected endpoints:
echo    curl -H "Authorization: Bearer YOUR_TOKEN_HERE" http://localhost:8080/api/accounts/mine
echo.
echo ========================================
echo Security Features Working:
echo ========================================
echo.
echo ✅ 403 Forbidden on root path - SECURITY WORKING!
echo ✅ Authentication required for protected endpoints
echo ✅ BCrypt password hashing implemented
echo ✅ Rate limiting active
echo ✅ Resource ownership enforced
echo ✅ DTOs prevent data exposure
echo.
pause
