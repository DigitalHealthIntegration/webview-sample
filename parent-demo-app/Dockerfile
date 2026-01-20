# ==========================
# STAGE 1: Build the React app
# ==========================
FROM node:18-alpine AS build

# Set working directory inside container
WORKDIR /app

# Copy package files first (better caching)
COPY package.json package-lock.json ./

# Install dependencies
RUN npm install

# Copy the rest of the project
COPY . .

# Build the Vite React app
RUN npm run build

# ==========================
# STAGE 2: Serve with Nginx
# ==========================
FROM nginx:alpine

# Remove default nginx static files
RUN rm -rf /usr/share/nginx/html/*

# Copy built React app from previous stage
COPY --from=build /app/dist /usr/share/nginx/html

# Copy a custom nginx config (we'll create this next)
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Expose port 80
EXPOSE 80

# Start nginx
CMD ["nginx", "-g", "daemon off;"]
