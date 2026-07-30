FROM nginx:alpine

# Copy static frontend assets directly to Nginx web root
COPY . /usr/share/nginx/html

# Remove the Dockerfile itself from served web assets
RUN rm -f /usr/share/nginx/html/Dockerfile

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
