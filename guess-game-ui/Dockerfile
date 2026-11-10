# Stage 1: Build code Vue ra HTML/JS/CSS
FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

# Stage 2: Dùng Nginx để chạy giao diện
FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
# COPY đè cấu hình tùy chỉnh vào Nginx
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]