# ---- Build Stage ----
FROM node:20-alpine AS build
WORKDIR /app

ARG API_URL=http://api-gateway:8080
ENV API_URL=${API_URL}
# Copy package files and install dependencies
COPY package.json package-lock.json ./
RUN npm ci

# Copy source and build
COPY . .
RUN npm run build   # Adjust for your Angular config

# ---- Runtime Stage ----
FROM nginx:alpine

# Copy built files from build stage
COPY --from=build /app/dist/cycle-time-ui/browser /usr/share/nginx/html

# Copy custom nginx config (optional)
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Expose port 80 (HTTP)
EXPOSE 80
