// https://gist.github.com/gkhays/e264009c0832c73d5345847e673a64ab
export default function drawSine(canvasId, fillColor) {
    let step;

    function drawPoint(ctx, x, y) {
        const radius = 2;
        ctx.beginPath();

        // Hold x constant at 4 so the point only moves up and down.
        ctx.arc(x - 5, y, radius, 0, 2 * Math.PI, false);

        ctx.fillStyle = fillColor || '#fff';
        ctx.fill();
        ctx.lineWidth = 1;
        ctx.stroke();
    }

    function plotSine(ctx, xOffset) {
        const width = ctx.canvas.width;
        const height = ctx.canvas.height;

        ctx.beginPath();
        ctx.lineWidth = 2;
        ctx.strokeStyle = fillColor || '#fff';

        // Drawing point

        let x = -2;
        let y = 0;
        const amplitude = 50;
        const frequency = 50;

        ctx.moveTo(x, 50);
        while (x <= width) {
            y = height / 2 + amplitude * Math.sin((x + xOffset) / frequency) * Math.cos((x + xOffset) / (frequency * 0.54515978463));
            ctx.lineTo(x, y);
            x += 5;
        }
        ctx.stroke();
        ctx.save();
        drawPoint(ctx, x, y);

        ctx.stroke();
        ctx.restore();
    }

    function draw() {
        const canvas = document.getElementById(canvasId);
        if (canvas == null) return;
        const context = canvas.getContext("2d");

        context.clearRect(0, 0, 1000, 150);
        context.save();

        plotSine(context, step);
        context.restore();

        step += 0.5;
        window.requestAnimationFrame(draw);
    }

    function fix_dpi() {
        const canvas = document.getElementById(canvasId);
        if (canvas == null) return;
        let dpi = window.devicePixelRatio;
        canvas.getContext('2d');
        const style_width = getComputedStyle(canvas).getPropertyValue("width").slice(0, -2);
        // Scale the canvas
        canvas.setAttribute('width', `${style_width * dpi}`);
    }

    fix_dpi();
    step = -1;
    window.requestAnimationFrame(draw);
}