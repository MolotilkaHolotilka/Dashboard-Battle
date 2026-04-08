# Этап 1: сборка
FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
ARG VITE_ENABLE_DANGER_ZONE=false
ENV VITE_ENABLE_DANGER_ZONE=$VITE_ENABLE_DANGER_ZONE
RUN npm run build

# Этап 2: раздача статики через nginx
FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
